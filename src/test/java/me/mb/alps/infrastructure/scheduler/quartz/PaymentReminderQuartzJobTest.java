package me.mb.alps.infrastructure.scheduler.quartz;

import me.mb.alps.application.port.out.RepaymentSchedulePersistencePort;
import me.mb.alps.domain.entity.Customer;
import me.mb.alps.domain.entity.LoanApplication;
import me.mb.alps.domain.entity.LoanProduct;
import me.mb.alps.domain.entity.RepaymentSchedule;
import me.mb.alps.domain.enums.LoanStatus;
import me.mb.alps.domain.enums.PaymentStatus;
import me.mb.alps.infrastructure.scheduler.PaymentReminderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PaymentReminderQuartzJobTest {

    @Mock
    private RepaymentSchedulePersistencePort schedulePort;

    @Mock
    private PaymentReminderService reminderService;

    @Mock
    private JobExecutionContext jobExecutionContext;

    @InjectMocks
    private PaymentReminderQuartzJob job;

    private RepaymentSchedule schedule1;
    private RepaymentSchedule schedule2;

    @BeforeEach
    void setUp() {
        Customer customer = Customer.builder()
                .id(UUID.randomUUID())
                .civilId("C001")
                .fullName("Nguyen Van A")
                .email("a@example.com")
                .build();

        LoanProduct product = LoanProduct.builder()
                .id(UUID.randomUUID())
                .code("PROD1")
                .name("Vay tieu dung")
                .build();

        LoanApplication loanApplication = LoanApplication.builder()
                .id(UUID.randomUUID())
                .customer(customer)
                .product(product)
                .amount(new BigDecimal("10000000"))
                .termMonths(12)
                .status(LoanStatus.APPROVED)
                .build();

        schedule1 = RepaymentSchedule.builder()
                .id(UUID.randomUUID())
                .loanApplication(loanApplication)
                .installmentNumber(1)
                .dueDate(LocalDate.now().plusDays(3))
                .principalAmount(new BigDecimal("800000"))
                .interestAmount(new BigDecimal("100000"))
                .totalAmount(new BigDecimal("900000"))
                .status(PaymentStatus.PENDING)
                .build();

        schedule2 = RepaymentSchedule.builder()
                .id(UUID.randomUUID())
                .loanApplication(loanApplication)
                .installmentNumber(2)
                .dueDate(LocalDate.now().plusDays(3))
                .principalAmount(new BigDecimal("810000"))
                .interestAmount(new BigDecimal("90000"))
                .totalAmount(new BigDecimal("900000"))
                .status(PaymentStatus.PENDING)
                .build();
    }

    @Test
    void execute_findsUpcomingPayments_andSendsReminders() throws JobExecutionException {
        LocalDate targetDate = LocalDate.now().plusDays(3);
        when(schedulePort.findByDueDateBetweenAndStatus(
                targetDate, targetDate, PaymentStatus.PENDING
        )).thenReturn(List.of(schedule1, schedule2));

        job.execute(jobExecutionContext);

        verify(schedulePort).findByDueDateBetweenAndStatus(targetDate, targetDate, PaymentStatus.PENDING);
        verify(reminderService).sendReminder(schedule1);
        verify(reminderService).sendReminder(schedule2);
    }

    @Test
    void execute_noUpcomingPayments_doesNotSendReminders() throws JobExecutionException {
        LocalDate targetDate = LocalDate.now().plusDays(3);
        when(schedulePort.findByDueDateBetweenAndStatus(
                targetDate, targetDate, PaymentStatus.PENDING
        )).thenReturn(List.of());

        job.execute(jobExecutionContext);

        verify(schedulePort).findByDueDateBetweenAndStatus(targetDate, targetDate, PaymentStatus.PENDING);
        verify(reminderService, never()).sendReminder(any());
    }

    @Test
    void execute_reminderServiceThrowsException_continuesWithNextSchedule() {
        LocalDate targetDate = LocalDate.now().plusDays(3);
        when(schedulePort.findByDueDateBetweenAndStatus(
                targetDate, targetDate, PaymentStatus.PENDING
        )).thenReturn(List.of(schedule1, schedule2));

        doThrow(new RuntimeException("Email service unavailable"))
                .when(reminderService).sendReminder(schedule1);
        doNothing().when(reminderService).sendReminder(schedule2);

        // Should not throw exception, should continue processing
        assertThatCode(() -> job.execute(jobExecutionContext))
                .doesNotThrowAnyException();

        verify(reminderService).sendReminder(schedule1);
        verify(reminderService).sendReminder(schedule2);
    }

    @Test
    void execute_allRemindersFail_doesNotThrowException() {
        LocalDate targetDate = LocalDate.now().plusDays(3);
        when(schedulePort.findByDueDateBetweenAndStatus(
                targetDate, targetDate, PaymentStatus.PENDING
        )).thenReturn(List.of(schedule1, schedule2));

        doThrow(new RuntimeException("Service error"))
                .when(reminderService).sendReminder(any());

        // Should not throw exception even if all reminders fail
        assertThatCode(() -> job.execute(jobExecutionContext))
                .doesNotThrowAnyException();

        verify(reminderService, times(2)).sendReminder(any());
    }
}
