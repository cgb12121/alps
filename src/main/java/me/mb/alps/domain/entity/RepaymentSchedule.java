package me.mb.alps.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.mb.alps.domain.enums.PaymentStatus;
import me.mb.alps.domain.exception.DomainException;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Pre-computed: Lịch trả nợ được tính sẵn khi giải ngân (Rich Domain).
 * Status và paidAmount chỉ thay đổi qua recordPayment() / markOverdue().
 */
@Entity
@Table(name = "repayment_schedules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RepaymentSchedule {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
    @Setter(AccessLevel.NONE)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "loan_application_id", nullable = false)
    private LoanApplication loanApplication;

    /** Kỳ số: 1, 2, 3... N (termMonths). */
    @Column(name = "installment_number", nullable = false)
    private int installmentNumber;

    /** Ngày đến hạn trả (VD: 15/03/2025). */
    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    /** Tiền gốc phải trả kỳ này. */
    @Column(name = "principal_amount", precision = 19, scale = 4, nullable = false)
    private BigDecimal principalAmount;

    /** Tiền lãi phải trả kỳ này. */
    @Column(name = "interest_amount", precision = 19, scale = 4, nullable = false)
    private BigDecimal interestAmount;

    /** Tiền phạt chậm trả (cộng dồn). Thay đổi chỉ qua applyPenalty(). */
    @Column(name = "penalty_amount", precision = 19, scale = 4)
    @Builder.Default
    @Setter(AccessLevel.NONE)
    private BigDecimal penaltyAmount = BigDecimal.ZERO;

    /** Phí dịch vụ/thu hộ (nếu có). */
    @Column(name = "fee_amount", precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal feeAmount = BigDecimal.ZERO;

    /** Tổng phải trả (principal + interest). */
    @Column(name = "total_amount", precision = 19, scale = 4, nullable = false)
    private BigDecimal totalAmount;

    /** Số tiền đã trả (có thể < totalAmount nếu PARTIALLY_PAID). */
    @Column(name = "paid_amount", precision = 19, scale = 4)
    @Builder.Default
    @Setter(AccessLevel.NONE)
    private BigDecimal paidAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    @Builder.Default
    @Setter(AccessLevel.NONE)
    private PaymentStatus status = PaymentStatus.PENDING;

    /** Ngày thanh toán thực tế (khi status = PAID). */
    @Column(name = "paid_date")
    private LocalDate paidDate;

    // --- Rich behaviour ---

    /**
     * Ghi nhận thanh toán (một phần hoặc đủ kỳ). Chỉ áp dụng khi PENDING hoặc PARTIALLY_PAID.
     *
     * @param amount   số tiền thanh toán (phải &lt;= số còn lại)
     * @param paidDate ngày thanh toán (thường LocalDate.now())
     */
    public void recordPayment(BigDecimal amount, LocalDate paidDate) {
        if (status != PaymentStatus.PENDING && status != PaymentStatus.PARTIALLY_PAID) {
            throw new DomainException(
                    "Chỉ được ghi nhận thanh toán cho kỳ PENDING hoặc PARTIALLY_PAID. Hiện tại: " + status);
        }
        BigDecimal remaining = totalAmount.subtract(paidAmount);
        if (amount.compareTo(BigDecimal.ZERO) <= 0 || amount.compareTo(remaining) > 0) {
            throw new DomainException(
                    "Số tiền thanh toán phải dương và không vượt quá số còn lại: " + remaining);
        }
        this.paidAmount = this.paidAmount.add(amount);
        if (this.paidAmount.compareTo(totalAmount) >= 0) {
            this.status = PaymentStatus.PAID;
            this.paidDate = paidDate;
        } else {
            this.status = PaymentStatus.PARTIALLY_PAID;
        }
    }

    /**
     * Đánh dấu quá hạn. Chỉ áp dụng khi PENDING.
     */
    public void markOverdue() {
        if (this.status != PaymentStatus.PENDING) {
            throw new DomainException("Chỉ được đánh dấu quá hạn cho kỳ PENDING. Hiện tại: " + this.status);
        }
        this.status = PaymentStatus.OVERDUE;
    }

    /** Số tiền còn phải trả cho kỳ này. */
    public BigDecimal getRemainingAmount() {
        return totalAmount.subtract(paidAmount);
    }

    /**
     * Áp dụng tiền phạt trễ hạn (cộng dồn) và cập nhật totalAmount = principal + interest + penalty + fee.
     */
    public void applyPenalty(BigDecimal penalty) {
        if (penalty == null || penalty.compareTo(BigDecimal.ZERO) < 0) {
            throw new DomainException("Tiền phạt phải >= 0");
        }
        BigDecimal current = penaltyAmount != null ? penaltyAmount : BigDecimal.ZERO;
        this.penaltyAmount = current.add(penalty);
        BigDecimal fee = feeAmount != null ? feeAmount : BigDecimal.ZERO;
        this.totalAmount = principalAmount.add(interestAmount).add(this.penaltyAmount).add(fee);
    }
}
