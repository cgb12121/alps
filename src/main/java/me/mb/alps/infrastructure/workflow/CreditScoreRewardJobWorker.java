package me.mb.alps.infrastructure.workflow;

import io.camunda.client.annotation.JobWorker;
import io.camunda.client.api.response.ActivatedJob;
import io.camunda.client.api.worker.JobClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.mb.alps.application.port.out.LoanApplicationPersistencePort;
import me.mb.alps.application.port.out.UpdateCustomerCreditScorePort;
import me.mb.alps.domain.entity.LoanApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

/**
 * JobWorker: Xử lý cộng điểm CIC khi trả đúng hạn.
 * Logic: Chỉ cộng điểm nếu vay lớn (>= 20tr) và thời gian vay >= 12 tháng.
 */
@SuppressWarnings("unused")
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnBooleanProperty(name = "camunda.client.enabled")
public class CreditScoreRewardJobWorker {

    private static final BigDecimal MIN_LOAN_AMOUNT_FOR_REWARD = new BigDecimal("20000000"); // 20tr
    private static final int MIN_TERM_MONTHS_FOR_REWARD = 12;

    private final LoanApplicationPersistencePort loanPort;
    private final UpdateCustomerCreditScorePort creditScorePort;

    @JobWorker(type = "check-loan-criteria")
    public void checkLoanCriteria(JobClient jobClient, ActivatedJob job) {
        try {
            Map<String, Object> variables = job.getVariablesAsMap();
            String loanIdStr = (String) variables.get("loanApplicationId");

            UUID loanId = UUID.fromString(loanIdStr);
            LoanApplication loan = loanPort.findById(loanId)
                    .orElseThrow(() -> new IllegalStateException("Loan not found: " + loanId));

            BigDecimal amount = loan.getAmount();
            int termMonths = loan.getTermMonths();

            boolean eligibleForReward = amount.compareTo(MIN_LOAN_AMOUNT_FOR_REWARD) >= 0
                    && termMonths >= MIN_TERM_MONTHS_FOR_REWARD;

            log.info("Checked loan criteria for loan {}: amount={}, termMonths={}, eligible={}",
                    loanId, amount, termMonths, eligibleForReward);

            jobClient.newCompleteCommand(job.getKey())
                    .variables(Map.of(
                            "eligibleForReward", eligibleForReward,
                            "loanAmount", amount.toString(),
                            "termMonths", termMonths
                    ))
                    .send()
                    .join();
        } catch (Exception e) {
            log.error("Failed to check loan criteria: {}", e.getMessage());
            jobClient.newFailCommand(job.getKey())
                    .retries(job.getRetries() - 1)
                    .errorMessage(e.getMessage())
                    .send();
        }
    }

    @JobWorker(type = "apply-credit-score-reward")
    public void applyCreditScoreReward(JobClient jobClient, ActivatedJob job) {
        try {
            Map<String, Object> variables = job.getVariablesAsMap();
            String loanIdStr = (String) variables.get("loanApplicationId");

            UUID loanId = UUID.fromString(loanIdStr);
            LoanApplication loan = loanPort.findById(loanId)
                    .orElseThrow(() -> new IllegalStateException("Loan not found: " + loanId));

            UUID customerId = loan.getCustomer().getId();
            BigDecimal amount = loan.getAmount();
            int termMonths = loan.getTermMonths();

            // Tính điểm thưởng: dựa trên số tiền và thời gian vay
            // Vay >= 50tr và >= 24 tháng: +20 điểm
            // Vay >= 30tr và >= 18 tháng: +15 điểm
            // Vay >= 20tr và >= 12 tháng: +10 điểm
            int rewardPoints = 0;
            if (amount.compareTo(new BigDecimal("50000000")) >= 0 && termMonths >= 24) {
                rewardPoints = 20;
            } else if (amount.compareTo(new BigDecimal("30000000")) >= 0 && termMonths >= 18) {
                rewardPoints = 15;
            } else if (amount.compareTo(MIN_LOAN_AMOUNT_FOR_REWARD) >= 0 && termMonths >= MIN_TERM_MONTHS_FOR_REWARD) {
                rewardPoints = 10;
            }

            if (rewardPoints > 0) {
                int currentScore = creditScorePort.getCreditScore(customerId);
                int newScore = Math.min(850, currentScore + rewardPoints); // Tối đa 850 điểm

                creditScorePort.updateCreditScore(customerId, newScore);

                log.info("Applied credit score reward for loan {}: +{} points ({} -> {})",
                        loanId, rewardPoints, currentScore, newScore);

                jobClient.newCompleteCommand(job.getKey())
                        .variables(Map.of(
                                "rewardApplied", true,
                                "pointsAdded", rewardPoints,
                                "newCreditScore", newScore
                        ))
                        .send()
                        .join();
            } else {
                log.warn("No reward points calculated for loan {}", loanId);
                jobClient.newCompleteCommand(job.getKey())
                        .variables(Map.of("rewardApplied", false))
                        .send()
                        .join();
            }
        } catch (Exception e) {
            log.error("Failed to apply credit score reward: {}", e.getMessage());
            jobClient.newFailCommand(job.getKey())
                    .retries(job.getRetries() - 1)
                    .errorMessage(e.getMessage())
                    .send();
        }
    }
}
