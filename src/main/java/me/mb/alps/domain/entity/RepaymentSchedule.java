package me.mb.alps.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.mb.alps.domain.enums.PaymentStatus;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Pre-computed: Lịch trả nợ được tính sẵn khi giải ngân (Disbursement).
 * Mỗi loan application có N dòng (N = termMonths). Đến ngày dueDate, hệ thống nhắc nợ.
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

    /** Tiền phạt chậm trả (cộng dồn). */
    @Column(name = "penalty_amount", precision = 19, scale = 4)
    @Builder.Default
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
    private BigDecimal paidAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;

    /** Ngày thanh toán thực tế (khi status = PAID). */
    @Column(name = "paid_date")
    private LocalDate paidDate;
}
