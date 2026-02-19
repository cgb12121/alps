package me.mb.alps.infrastructure.scheduler.quartz;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test để verify QuartzConfig beans được tạo đúng.
 * Spring Boot Quartz sẽ tự động đăng ký các JobDetail và Trigger beans.
 */
@ExtendWith(MockitoExtension.class)
@SpringBootTest(classes = {QuartzConfig.class, PaymentReminderQuartzJob.class, MarkOverdueQuartzJob.class})
@TestPropertySource(properties = {
        "spring.quartz.job-store-type=memory",
        "spring.quartz.jdbc.initialize-schema=never"
})
class QuartzConfigTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void paymentReminderJobDetail_isCreated() {
        JobDetail jobDetail = applicationContext.getBean("paymentReminderJobDetail", JobDetail.class);

        assertThat(jobDetail).isNotNull();
        assertThat(jobDetail.getKey().getName()).isEqualTo("paymentReminderJob");
        assertThat(jobDetail.getKey().getGroup()).isEqualTo("paymentJobs");
        assertThat(jobDetail.getJobClass()).isEqualTo(PaymentReminderQuartzJob.class);
        assertThat(jobDetail.isDurable()).isTrue();
    }

    @Test
    void paymentReminderTrigger_isCreated() {
        Trigger trigger = applicationContext.getBean("paymentReminderTrigger", Trigger.class);

        assertThat(trigger).isNotNull();
        assertThat(trigger.getKey().getName()).isEqualTo("paymentReminderTrigger");
        assertThat(trigger.getKey().getGroup()).isEqualTo("paymentTriggers");
        assertThat(trigger.getJobKey().getName()).isEqualTo("paymentReminderJob");
    }

    @Test
    void markOverdueJobDetail_isCreated() {
        JobDetail jobDetail = applicationContext.getBean("markOverdueJobDetail", JobDetail.class);

        assertThat(jobDetail).isNotNull();
        assertThat(jobDetail.getKey().getName()).isEqualTo("markOverdueJob");
        assertThat(jobDetail.getKey().getGroup()).isEqualTo("paymentJobs");
        assertThat(jobDetail.getJobClass()).isEqualTo(MarkOverdueQuartzJob.class);
        assertThat(jobDetail.isDurable()).isTrue();
    }

    @Test
    void markOverdueTrigger_isCreated() {
        Trigger trigger = applicationContext.getBean("markOverdueTrigger", Trigger.class);

        assertThat(trigger).isNotNull();
        assertThat(trigger.getKey().getName()).isEqualTo("markOverdueTrigger");
        assertThat(trigger.getKey().getGroup()).isEqualTo("paymentTriggers");
        assertThat(trigger.getJobKey().getName()).isEqualTo("markOverdueJob");
    }

    @Test
    void paymentReminderTrigger_hasCorrectCronExpression() {
        Trigger trigger = applicationContext.getBean("paymentReminderTrigger", Trigger.class);
        assertThat(trigger).isInstanceOf(CronTrigger.class);

        CronTrigger cronTrigger = (CronTrigger) trigger;
        assertThat(cronTrigger.getCronExpression()).isEqualTo("0 0 9 * * ?");
    }

    @Test
    void markOverdueTrigger_hasCorrectCronExpression() {
        Trigger trigger = applicationContext.getBean("markOverdueTrigger", Trigger.class);
        assertThat(trigger).isInstanceOf(CronTrigger.class);

        CronTrigger cronTrigger = (CronTrigger) trigger;
        assertThat(cronTrigger.getCronExpression()).isEqualTo("0 0 10 * * ?");
    }
}
