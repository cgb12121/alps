package me.mb.alps.infrastructure.scheduler.quartz;

import me.mb.alps.application.port.out.RepaymentSchedulePersistencePort;
import me.mb.alps.domain.entity.Customer;
import me.mb.alps.domain.entity.LoanApplication;
import me.mb.alps.domain.entity.LoanProduct;
import me.mb.alps.domain.entity.RepaymentSchedule;
import me.mb.alps.domain.enums.LoanStatus;
import me.mb.alps.domain.enums.PaymentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MarkOverdueQuartzJobTest {

    @Mock
    private RepaymentSchedulePersistencePort schedulePort;

    @Mock
    private JobExecutionContext jobExecutionContext;

    @InjectMocks
    private MarkOverdueQuartzJob job;

    private RepaymentSchedule overdueSchedule1;
    private RepaymentSchedule overdueSchedule2;

    @BeforeEach
    void setUp() {
        Customer customer = Customer.builder()
                .id(UUID.randomUUID())
                .civilId("C001")
                .fullName("Nguyen Van A")
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

        overdueSchedule1 = RepaymentSchedule.builder()
                .id(UUID.randomUUID())
                .loanApplication(loanApplication)
                .installmentNumber(1)
                .dueDate(LocalDate.now().minusDays(5))
                .principalAmount(new BigDecimal("800000"))
                .interestAmount(new BigDecimal("100000"))
                .totalAmount(new BigDecimal("900000"))
                .status(PaymentStatus.PENDING)
                .build();

        overdueSchedule2 = RepaymentSchedule.builder()
                .id(UUID.randomUUID())
                .loanApplication(loanApplication)
                .installmentNumber(2)
                .dueDate(LocalDate.now().minusDays(10))
                .principalAmount(new BigDecimal("810000"))
                .interestAmount(new BigDecimal("90000"))
                .totalAmount(new BigDecimal("900000"))
                .status(PaymentStatus.PENDING)
                .build();
    }

    @Test
    void execute_findsOverduePayments_andMarksThemOverdue() throws JobExecutionException {
        LocalDate today = LocalDate.now();
        LocalDate startDate = LocalDate.of(2020, 1, 1);
        LocalDate endDate = today.minusDays(1);

        when(schedulePort.findByDueDateBetweenAndStatus(
                startDate, endDate, PaymentStatus.PENDING
        )).thenReturn(List.of(overdueSchedule1, overdueSchedule2));

        when(schedulePort.save(any(RepaymentSchedule.class))).thenAnswer(inv -> inv.getArgument(0));

        job.execute(jobExecutionContext);

        verify(schedulePort).findByDueDateBetweenAndStatus(startDate, endDate, PaymentStatus.PENDING);

        ArgumentCaptor<RepaymentSchedule> captor = ArgumentCaptor.forClass(RepaymentSchedule.class);
        verify(schedulePort, times(2)).save(captor.capture());

        List<RepaymentSchedule> saved = captor.getAllValues();
        assertThat(saved).hasSize(2);
        assertThat(saved.get(0).getStatus()).isEqualTo(PaymentStatus.OVERDUE);
        assertThat(saved.get(1).getStatus()).isEqualTo(PaymentStatus.OVERDUE);
    }

    @Test
    void execute_noOverduePayments_doesNotSaveAnything() throws JobExecutionException {
        LocalDate today = LocalDate.now();
        LocalDate startDate = LocalDate.of(2020, 1, 1);
        LocalDate endDate = today.minusDays(1);

        when(schedulePort.findByDueDateBetweenAndStatus(
                startDate, endDate, PaymentStatus.PENDING
        )).thenReturn(List.of());

        job.execute(jobExecutionContext);

        verify(schedulePort).findByDueDateBetweenAndStatus(startDate, endDate, PaymentStatus.PENDING);
        verify(schedulePort, never()).save(any());
    }

    @Test
    void execute_setsStatusToOverdue_beforeSaving() throws JobExecutionException {
        LocalDate today = LocalDate.now();
        LocalDate startDate = LocalDate.of(2020, 1, 1);
        LocalDate endDate = today.minusDays(1);

        when(schedulePort.findByDueDateBetweenAndStatus(
                startDate, endDate, PaymentStatus.PENDING
        )).thenReturn(List.of(overdueSchedule1));

        when(schedulePort.save(any(RepaymentSchedule.class))).thenAnswer(inv -> {
            RepaymentSchedule saved = inv.getArgument(0);
            assertThat(saved.getStatus()).isEqualTo(PaymentStatus.OVERDUE);
            return saved;
        });

        job.execute(jobExecutionContext);

        verify(schedulePort).save(overdueSchedule1);
        assertThat(overdueSchedule1.getStatus()).isEqualTo(PaymentStatus.OVERDUE);
    }

    @Test
    void execute_usesCorrectDateRange() throws JobExecutionException {
        LocalDate today = LocalDate.now();
        LocalDate startDate = LocalDate.of(2020, 1, 1);
        LocalDate endDate = today.minusDays(1);

        when(schedulePort.findByDueDateBetweenAndStatus(
                startDate, endDate, PaymentStatus.PENDING
        )).thenReturn(List.of());

        job.execute(jobExecutionContext);

        verify(schedulePort).findByDueDateBetweenAndStatus(startDate, endDate, PaymentStatus.PENDING);
    }
}
