package me.mb.alps.infrastructure.scheduler.quartz;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.mb.alps.application.port.out.RepaymentSchedulePersistencePort;
import me.mb.alps.domain.entity.RepaymentSchedule;
import me.mb.alps.domain.enums.PaymentStatus;
import me.mb.alps.infrastructure.scheduler.PaymentReminderService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * Quartz Job: Quét RepaymentSchedule hàng ngày để nhắc nợ.
 * Chạy lúc 9:00 AM mỗi ngày, nhắc trước 3 ngày đến hạn.
 * <p>
 * Các app như MoMo, Home Credit thường nhắc vào đầu tháng (ngày 5-10) vì họ set dueDate cố định.
 * <p>
 * Dependencies được inject qua SpringBeanJobFactory (Spring Boot Quartz tự động config).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentReminderQuartzJob implements Job {

    private final RepaymentSchedulePersistencePort schedulePort;
    private final PaymentReminderService reminderService;

    @Override
    public void execute(JobExecutionContext context) {
        LocalDate targetDate = LocalDate.now().plusDays(3); // Nhắc trước 3 ngày
        log.info("PaymentReminderQuartzJob: Scanning for payments due on {}", targetDate);

        List<RepaymentSchedule> upcoming = schedulePort.findByDueDateBetweenAndStatus(
                targetDate, targetDate, PaymentStatus.PENDING
        );

        log.info("Found {} upcoming payments to remind", upcoming.size());
        for (RepaymentSchedule schedule : upcoming) {
            try {
                reminderService.sendReminder(schedule);
            } catch (Exception e) {
                log.error("Failed to send reminder for schedule {}: {}", schedule.getId(), e.getMessage());
            }
        }
    }
}
