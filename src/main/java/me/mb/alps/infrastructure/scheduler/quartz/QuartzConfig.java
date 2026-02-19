package me.mb.alps.infrastructure.scheduler.quartz;

import org.quartz.JobDetail;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;

/**
 * Cấu hình Quartz Jobs cho Payment Reminder và Mark Overdue.
 * Spring Boot Quartz tự động tạo JDBC JobStore nếu có datasource.
 * <p>
 * Dùng CronTrigger thay vì SimpleTrigger để chạy đúng giờ (9:00 AM, 10:00 AM) mỗi ngày.
 */
@Configuration
public class QuartzConfig {

    /**
     * JobDetail for PaymentReminderQuartzJob
     */
    @Bean
    public JobDetailFactoryBean paymentReminderJobDetail() {
        JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
        factoryBean.setJobClass(PaymentReminderQuartzJob.class);
        factoryBean.setName("paymentReminderJob");
        factoryBean.setGroup("paymentJobs");
        factoryBean.setDescription("Nhắc nợ trước 3 ngày đến hạn");
        factoryBean.setDurability(true);
        return factoryBean;
    }

    /**
     * CronTrigger for cho PaymentReminderQuartzJob
     */
    @Bean
    public CronTriggerFactoryBean paymentReminderTrigger(JobDetail paymentReminderJobDetail) {
        CronTriggerFactoryBean factoryBean = new CronTriggerFactoryBean();
        factoryBean.setJobDetail(paymentReminderJobDetail);
        factoryBean.setName("paymentReminderTrigger");
        factoryBean.setGroup("paymentTriggers");
        factoryBean.setCronExpression("0 0 9 * * ?"); // 9:00 AM daily
        return factoryBean;
    }

    /**
     * JobDetail for MarkOverdueQuartzJob :marking OVERDUE.
     */
    @Bean
    public JobDetailFactoryBean markOverdueJobDetail() {
        JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
        factoryBean.setJobClass(MarkOverdueQuartzJob.class);
        factoryBean.setName("markOverdueJob");
        factoryBean.setGroup("paymentJobs");
        factoryBean.setDescription("Đánh dấu các khoản quá hạn");
        factoryBean.setDurability(true);
        return factoryBean;
    }

    /**
     * CronTrigger for MarkOverdueQuartzJob
     */
    @Bean
    public CronTriggerFactoryBean markOverdueTrigger(JobDetail markOverdueJobDetail) {
        CronTriggerFactoryBean factoryBean = new CronTriggerFactoryBean();
        factoryBean.setJobDetail(markOverdueJobDetail);
        factoryBean.setName("markOverdueTrigger");
        factoryBean.setGroup("paymentTriggers");
        factoryBean.setCronExpression("0 0 10 * * ?"); // 10:00 AM daily
        return factoryBean;
    }
}
