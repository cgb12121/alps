package me.mb.alps.application.service.loan;

import me.mb.alps.domain.entity.LoanApplication;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Tính lịch trả nợ (Payment Schedule) cho Vay Plan (Term Loan / Trả góp).
 * Dùng công thức PMT (Payment) - Dư nợ giảm dần (Equal Installments).
 * <p>
 * Ví dụ: Vay 50tr, 12 tháng, lãi 10%/năm → Mỗi tháng trả ~4.4tr (gốc + lãi bằng nhau).
 */
@Service
public class LoanScheduleCalculator {

    private static final MathContext MC = new MathContext(10, RoundingMode.HALF_UP);

    /**
     * Tính lịch trả nợ cho loan application.
     * @param application Loan application (đã có amount, termMonths, interestRateAnnual)
     * @param firstPaymentDate Ngày trả đầu tiên (thường là ngày giải ngân + 1 tháng)
     * @return Danh sách các kỳ trả nợ
     */
    public List<PaymentScheduleItem> calculateSchedule(LoanApplication application, LocalDate firstPaymentDate) {
        BigDecimal principal = application.getAmount();
        int termMonths = application.getTermMonths();
        BigDecimal annualRate = application.getInterestRateAnnual() != null
                ? application.getInterestRateAnnual()
                : BigDecimal.ZERO;

        return calculateSchedule(principal, termMonths, annualRate, firstPaymentDate);
    }

    /**
     * Tính lịch trả nợ với PMT formula (Excel PMT function).
     * PMT = Principal * (r * (1+r)^n) / ((1+r)^n - 1)
     * với r = monthlyRate = annualRate / 12, n = termMonths
     */
    public List<PaymentScheduleItem> calculateSchedule(
            BigDecimal principal,
            int termMonths,
            BigDecimal annualRate,
            LocalDate firstPaymentDate
    ) {
        List<PaymentScheduleItem> schedule = new ArrayList<>();
        if (termMonths <= 0 || principal.compareTo(BigDecimal.ZERO) <= 0) {
            return schedule;
        }

        BigDecimal monthlyRate = annualRate.divide(BigDecimal.valueOf(100), MC)
                .divide(BigDecimal.valueOf(12), MC);
        
        BigDecimal monthlyPayment;
        // Xử lý trường hợp lãi suất = 0: chia đều gốc
        if (monthlyRate.compareTo(BigDecimal.ZERO) == 0) {
            monthlyPayment = principal.divide(BigDecimal.valueOf(termMonths), MC)
                    .setScale(2, RoundingMode.HALF_UP);
        } else {
            BigDecimal onePlusR = BigDecimal.ONE.add(monthlyRate);
            BigDecimal onePlusRPowerN = onePlusR.pow(termMonths, MC);
            // PMT = Principal * (r * (1+r)^n) / ((1+r)^n - 1)
            BigDecimal numerator = monthlyRate.multiply(onePlusRPowerN, MC);
            BigDecimal denominator = onePlusRPowerN.subtract(BigDecimal.ONE, MC);
            monthlyPayment = principal.multiply(numerator, MC)
                    .divide(denominator, MC)
                    .setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal remainingPrincipal = principal;
        LocalDate paymentDate = firstPaymentDate;

        for (int period = 1; period <= termMonths; period++) {
            BigDecimal interest = remainingPrincipal.multiply(monthlyRate, MC)
                    .setScale(2, RoundingMode.HALF_UP);
            BigDecimal principalPayment = monthlyPayment.subtract(interest, MC)
                    .setScale(2, RoundingMode.HALF_UP);

            // Last period: adjust để tổng = principal ban đầu
            if (period == termMonths) {
                principalPayment = remainingPrincipal;
                monthlyPayment = principalPayment.add(interest);
            }

            remainingPrincipal = remainingPrincipal.subtract(principalPayment, MC)
                    .setScale(2, RoundingMode.HALF_UP);
            if (remainingPrincipal.compareTo(BigDecimal.ZERO) < 0) {
                remainingPrincipal = BigDecimal.ZERO;
            }

            schedule.add(new PaymentScheduleItem(
                    period,
                    paymentDate,
                    principalPayment,
                    interest,
                    monthlyPayment,
                    remainingPrincipal
            ));

            paymentDate = paymentDate.plusMonths(1);
        }

        return schedule;
    }

    /**
     * Một kỳ trả nợ trong lịch.
     */
    public record PaymentScheduleItem(
            int period,                    // Kỳ thứ mấy (1, 2, 3...)
            LocalDate paymentDate,         // Ngày đến hạn trả
            BigDecimal principalAmount,    // Tiền gốc kỳ này
            BigDecimal interestAmount,     // Tiền lãi kỳ này
            BigDecimal totalPayment,       // Tổng phải trả (gốc + lãi)
            BigDecimal remainingPrincipal  // Dư nợ còn lại sau kỳ này
    ) {}
}
