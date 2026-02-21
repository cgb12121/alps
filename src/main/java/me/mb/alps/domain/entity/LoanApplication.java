package me.mb.alps.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.mb.alps.domain.enums.LoanStatus;
import me.mb.alps.domain.exception.DomainException;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Aggregate root: loan application lifecycle (Rich Domain).
 * Status changes only through explicit behaviour methods; invariants enforced here.
 */
@Entity
@Table(name = "loan_applications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class LoanApplication {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
    @Setter(AccessLevel.NONE)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private LoanProduct product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submitted_by")
    private User submittedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private User reviewedBy;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(name = "term_months", nullable = false)
    private int termMonths;

    /** Lãi suất thực tế được áp dụng (%/năm). Do Drools risk-based pricing quyết định dựa trên risk score. */
    @Column(name = "interest_rate_annual", precision = 9, scale = 4)
    private BigDecimal interestRateAnnual;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    @Builder.Default
    @Setter(AccessLevel.NONE)
    private LoanStatus status = LoanStatus.DRAFT;

    /** Camunda Zeebe process instance key – links this record to the running process. */
    @Column(name = "process_instance_key")
    @Setter(AccessLevel.NONE)
    private Long processInstanceKey;

    @Version
    private long version;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // --- Rich behaviour: status changes only through these methods ---

    /**
     * Completes manual approval: only valid when status is REVIEW_REQUIRED.
     *
     * @param approved true = APPROVED, false = REJECTED
     */
    public void completeManualApproval(boolean approved, User reviewer, LocalDateTime reviewedAt) {
        if (this.status != LoanStatus.REVIEW_REQUIRED) {
            throw new DomainException(
                    "Chỉ được duyệt/thu hồi đơn đang chờ duyệt thủ công. Hiện tại: " + this.status);
        }
        this.status = approved ? LoanStatus.APPROVED : LoanStatus.REJECTED;
        this.reviewedBy = reviewer;
        this.reviewedAt = reviewedAt;
    }

    /**
     * Applies scoring result (Drools): only valid when application is SUBMITTED.
     * Sets status to APPROVED, REJECTED or REVIEW_REQUIRED and stores interest rate when approved.
     */
    public void applyScoringResult(LoanStatus newStatus, BigDecimal interestRateAnnual) {
        if (this.status != LoanStatus.SUBMITTED) {
            throw new DomainException(
                    "Chỉ có thể áp dụng kết quả chấm điểm khi đơn ở trạng thái SUBMITTED. Hiện tại: " + this.status);
        }
        if (newStatus != LoanStatus.APPROVED && newStatus != LoanStatus.REJECTED && newStatus != LoanStatus.REVIEW_REQUIRED) {
            throw new DomainException("Kết quả chấm điểm phải là APPROVED, REJECTED hoặc REVIEW_REQUIRED: " + newStatus);
        }
        this.status = newStatus;
        this.interestRateAnnual = interestRateAnnual;
    }

    /** Gắn process instance key từ Camunda sau khi start process. */
    public void linkProcessInstance(long processInstanceKey) {
        this.processInstanceKey = processInstanceKey;
    }
}
