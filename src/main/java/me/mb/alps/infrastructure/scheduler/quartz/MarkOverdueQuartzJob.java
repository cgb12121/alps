package me.mb.alps.infrastructure.scheduler.quartz;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.mb.alps.application.port.out.RepaymentSchedulePersistencePort;
import me.mb.alps.application.port.out.StartProcessPort;
import me.mb.alps.domain.entity.RepaymentSchedule;
import me.mb.alps.domain.enums.PaymentStatus;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Quartz Job: Đánh dấu OVERDUE cho các schedule quá hạn chưa trả.
 * Chạy lúc 10:00 AM mỗi ngày.
 * <p>
 * Dependencies được inject qua SpringBeanJobFactory (Spring Boot Quartz tự động config).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MarkOverdueQuartzJob implements Job {

    private final RepaymentSchedulePersistencePort schedulePort;
    @Lazy private final StartProcessPort startProcessPort;

    @Override
    public void execute(JobExecutionContext context) {
        LocalDate today = LocalDate.now();
        log.info("MarkOverdueQuartzJob: Marking overdue payments before {}", today);

        List<RepaymentSchedule> overdue = schedulePort.findByDueDateBetweenAndStatus(
                LocalDate.of(2020, 1, 1), today.minusDays(1), PaymentStatus.PENDING
        );

        log.info("Found {} overdue payments", overdue.size());
        for (RepaymentSchedule schedule : overdue) {
            schedule.markOverdue();
            schedulePort.save(schedule);
            log.info("Marked schedule {} as OVERDUE", schedule.getId());

            // Trigger Camunda overdue penalty workflow
            if (startProcessPort != null) {
                try {
                    long processKey = startProcessPort.startProcess(
                            "overdue-penalty",
                            Map.of("repaymentScheduleId", schedule.getId().toString())
                    );
                    log.info("Started overdue-penalty process for schedule {} -> processInstanceKey={}",
                            schedule.getId(), processKey);
                } catch (Exception e) {
                    log.error("Failed to start overdue-penalty process for schedule {}: {}",
                            schedule.getId(), e.getMessage());
                }
            }
        }
    }
}
