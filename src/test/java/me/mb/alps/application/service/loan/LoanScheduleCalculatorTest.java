package me.mb.alps.application.service.loan;

import me.mb.alps.domain.entity.Customer;
import me.mb.alps.domain.entity.LoanApplication;
import me.mb.alps.domain.entity.LoanProduct;
import me.mb.alps.domain.enums.LoanStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class LoanScheduleCalculatorTest {

    @InjectMocks
    private LoanScheduleCalculator calculator;

    @Test
    void calculateSchedule_12Months_10Percent_returns12Payments() {
        BigDecimal principal = new BigDecimal("50000000"); // 50tr
        int termMonths = 12;
        BigDecimal annualRate = new BigDecimal("10.0"); // 10%/năm
        LocalDate firstPayment = LocalDate.of(2025, 3, 15);

        List<LoanScheduleCalculator.PaymentScheduleItem> schedule = calculator.calculateSchedule(
                principal, termMonths, annualRate, firstPayment
        );

        assertThat(schedule).hasSize(12);
        assertThat(schedule.getFirst().period()).isEqualTo(1);
        assertThat(schedule.getFirst().paymentDate()).isEqualTo(firstPayment);
        assertThat(schedule.getFirst().principalAmount()).isPositive();
        assertThat(schedule.getFirst().interestAmount()).isPositive();
        assertThat(schedule.getFirst().totalPayment()).isPositive();
        // Tổng gốc phải bằng principal ban đầu
        BigDecimal totalPrincipal = schedule.stream()
                .map(LoanScheduleCalculator.PaymentScheduleItem::principalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertThat(totalPrincipal).isEqualByComparingTo(principal);
        // Kỳ cuối: remainingPrincipal = 0
        assertThat(schedule.get(11).remainingPrincipal()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void calculateSchedule_fromLoanApplication_works() {
        var customer = Customer.builder().id(UUID.randomUUID()).civilId("c1").fullName("A").build();
        var product = LoanProduct.builder().id(UUID.randomUUID()).code("P1").name("Vay")
                .minAmount(BigDecimal.ONE).maxAmount(BigDecimal.TEN).minTermMonths(6).maxTermMonths(36)
                .interestRateAnnual(BigDecimal.ZERO).active(true).build();
        var application = LoanApplication.builder()
                .id(UUID.randomUUID())
                .customer(customer)
                .product(product)
                .amount(new BigDecimal("30000000"))
                .termMonths(6)
                .interestRateAnnual(new BigDecimal("12.0"))
                .status(LoanStatus.APPROVED)
                .build();

        List<LoanScheduleCalculator.PaymentScheduleItem> schedule = calculator.calculateSchedule(
                application, LocalDate.of(2025, 3, 15)
        );

        assertThat(schedule).hasSize(6);
        assertThat(schedule.getFirst().totalPayment()).isPositive();
    }

    @Test
    void calculateSchedule_zeroRate_principalOnly() {
        BigDecimal principal = new BigDecimal("10000000");
        List<LoanScheduleCalculator.PaymentScheduleItem> schedule = calculator.calculateSchedule(
                principal, 3, BigDecimal.ZERO, LocalDate.now()
        );

        assertThat(schedule).hasSize(3);
        assertThat(schedule.getFirst().interestAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(schedule.getFirst().principalAmount()).isEqualByComparingTo(
                principal.divide(BigDecimal.valueOf(3), 2, java.math.RoundingMode.HALF_UP)
        );
    }
}
