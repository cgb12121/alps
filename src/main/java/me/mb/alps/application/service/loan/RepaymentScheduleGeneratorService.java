package me.mb.alps.application.service.loan;

import lombok.RequiredArgsConstructor;
import me.mb.alps.application.port.out.RepaymentSchedulePersistencePort;
import me.mb.alps.domain.entity.LoanApplication;
import me.mb.alps.domain.entity.RepaymentSchedule;
import me.mb.alps.domain.enums.PaymentStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Generate và lưu RepaymentSchedule (pre-computed) khi loan được giải ngân (disbursed).
 * Dùng LoanScheduleCalculator để tính PMT, sau đó persist vào DB.
 */
@Service
@RequiredArgsConstructor
public class RepaymentScheduleGeneratorService {

    private final LoanScheduleCalculator calculator;
    private final RepaymentSchedulePersistencePort persistencePort;

    /**
     * Sinh lịch trả nợ cho loan application và lưu vào DB.
     * @param loanApplication Loan đã được approve và có interestRateAnnual
     * @param disbursementDate Ngày giải ngân (kỳ 1 sẽ trả vào disbursementDate + 1 tháng)
     */
    @Transactional
    public List<RepaymentSchedule> generateAndSave(LoanApplication loanApplication, LocalDate disbursementDate) {
        // Tính lịch trả nợ (kỳ 1 bắt đầu từ disbursementDate + 1 tháng)
        LocalDate firstPaymentDate = disbursementDate.plusMonths(1);
        var scheduleItems = calculator.calculateSchedule(loanApplication, firstPaymentDate);

        // Convert sang RepaymentSchedule entity và lưu
        List<RepaymentSchedule> schedules = scheduleItems.stream()
                .map(item -> RepaymentSchedule.builder()
                        .loanApplication(loanApplication)
                        .installmentNumber(item.period())
                        .dueDate(item.paymentDate())
                        .principalAmount(item.principalAmount())
                        .interestAmount(item.interestAmount())
                        .totalAmount(item.totalPayment())
                        .paidAmount(BigDecimal.ZERO)
                        .status(PaymentStatus.PENDING)
                        .build())
                .toList();

        return persistencePort.saveAll(schedules);
    }
}
