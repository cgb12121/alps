package me.mb.alps.infrastructure.workflow;

import io.camunda.client.annotation.JobWorker;
import io.camunda.client.api.response.ActivatedJob;
import io.camunda.client.api.worker.JobClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.mb.alps.application.port.out.LoadCustomerPort;
import me.mb.alps.application.port.out.RepaymentSchedulePersistencePort;
import me.mb.alps.application.port.out.UpdateCustomerCreditScorePort;
import me.mb.alps.domain.entity.RepaymentSchedule;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

/**
 * JobWorker: Xử lý phạt khi quá hạn trả nợ.
 */
@SuppressWarnings("unused")
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnBooleanProperty(name = "camunda.client.enabled")
public class OverduePenaltyJobWorker {

    private final RepaymentSchedulePersistencePort schedulePort;
    private final UpdateCustomerCreditScorePort creditScorePort;
    private final LoadCustomerPort loadCustomerPort;

    @JobWorker(type = "calculate-overdue-days")
    public void calculateOverdueDays(JobClient jobClient, ActivatedJob job) {
        try {
            Map<String, Object> variables = job.getVariablesAsMap();
            String scheduleIdStr = (String) variables.get("repaymentScheduleId");
            if (scheduleIdStr == null) {
                jobClient.newFailCommand(job.getKey())
                        .retries(0)
                        .errorMessage("repaymentScheduleId required")
                        .send();
                return;
            }

            UUID scheduleId = UUID.fromString(scheduleIdStr);
            RepaymentSchedule schedule = schedulePort.findById(scheduleId)
                    .orElseThrow(() -> new IllegalStateException("Schedule not found: " + scheduleId));

            LocalDate dueDate = schedule.getDueDate();
            LocalDate today = LocalDate.now();
            long overdueDays = java.time.temporal.ChronoUnit.DAYS.between(dueDate, today);

            jobClient.newCompleteCommand(job.getKey())
                    .variables(Map.of("overdueDays", overdueDays))
                    .send()
                    .join();

            log.info("Calculated overdue days for schedule {}: {} days", scheduleId, overdueDays);
        } catch (Exception e) {
            log.error("Failed to calculate overdue days: {}", e.getMessage());
            jobClient.newFailCommand(job.getKey())
                    .retries(job.getRetries() - 1)
                    .errorMessage(e.getMessage())
                    .send();
        }
    }

    @JobWorker(type = "apply-light-penalty")
    public void applyLightPenalty(JobClient jobClient, ActivatedJob job) {
        try {
            Map<String, Object> variables = job.getVariablesAsMap();
            String scheduleIdStr = (String) variables.get("repaymentScheduleId");

            // Phạt nhẹ: chỉ ghi log, không trừ điểm
            log.info("Applied light penalty for schedule {}", scheduleIdStr);

            jobClient.newCompleteCommand(job.getKey())
                    .variables(Map.of("penaltyApplied", true, "penaltyType", "LIGHT"))
                    .send()
                    .join();
        } catch (Exception e) {
            log.error("Failed to apply light penalty: {}", e.getMessage());
            jobClient.newFailCommand(job.getKey())
                    .retries(job.getRetries() - 1)
                    .errorMessage(e.getMessage())
                    .send();
        }
    }

    @JobWorker(type = "apply-heavy-penalty")
    public void applyHeavyPenalty(JobClient jobClient, ActivatedJob job) {
        try {
            Map<String, Object> variables = job.getVariablesAsMap();
            String scheduleIdStr = (String) variables.get("repaymentScheduleId");

            UUID scheduleId = UUID.fromString(scheduleIdStr);
            RepaymentSchedule schedule = schedulePort.findById(scheduleId)
                    .orElseThrow(() -> new IllegalStateException("Schedule not found: " + scheduleId));

            UUID customerId = schedule.getLoanApplication().getCustomer().getId();

            long overdueDays = ((Number) variables.getOrDefault("overdueDays", 1L)).longValue();

            // 1) Tính tiền phạt:
            // - 3 ngày đầu: phạt cứng 200k
            // - Sau đó: % trên phần còn nợ, nhưng có trần để tránh "giết" khách
            BigDecimal penalty = getPenalty(overdueDays, schedule);

            schedule.applyPenalty(penalty);
            schedulePort.save(schedule);

            // 2) Phạt CIC: trừ điểm nhưng có trần và không tụt dưới 300
            int currentScore = creditScorePort.getCreditScore(customerId);
            int penaltyPoints = (int) Math.min(50, Math.max(10, overdueDays * 5)); // 10-50 điểm
            int newScore = Math.max(300, currentScore - penaltyPoints); // tối thiểu 300 điểm

            creditScorePort.updateCreditScore(customerId, newScore);

            log.info("Applied heavy penalty for schedule {}: moneyPenalty={}, deducted {} CIC points ({} -> {})",
                    scheduleId, penalty, penaltyPoints, currentScore, newScore);

            jobClient.newCompleteCommand(job.getKey())
                    .variables(Map.of(
                            "penaltyApplied", true,
                            "penaltyType", "HEAVY",
                            "moneyPenalty", penalty,
                            "pointsDeducted", penaltyPoints,
                            "newCreditScore", newScore
                    ))
                    .send()
                    .join();
        } catch (Exception e) {
            log.error("Failed to apply heavy penalty: {}", e.getMessage());
            jobClient.newFailCommand(job.getKey())
                    .retries(job.getRetries() - 1)
                    .errorMessage(e.getMessage())
                    .send();
        }
    }

    private static BigDecimal getPenalty(long overdueDays, RepaymentSchedule schedule) {
        BigDecimal penalty;
        BigDecimal TWO_HUNDRED_K = new BigDecimal("200000");

        if (overdueDays <= 3) {
            penalty = TWO_HUNDRED_K;
        } else {
            // Ví dụ: 1%/ngày trên số tiền chưa trả, tối đa 30% dư nợ
            BigDecimal remaining = schedule.getTotalAmount().subtract(schedule.getPaidAmount() != null
                    ? schedule.getPaidAmount()
                    : BigDecimal.ZERO);
            BigDecimal dailyRate = new BigDecimal("0.01"); // 1%/ngày
            BigDecimal days = BigDecimal.valueOf(overdueDays);
            BigDecimal variablePenalty = remaining.multiply(dailyRate).multiply(days);
            BigDecimal cap = remaining.multiply(new BigDecimal("0.30")); // trần 30% dư nợ
            penalty = variablePenalty.min(cap).max(TWO_HUNDRED_K); // luôn >= 200k
        }
        return penalty;
    }
}
