package me.mb.alps.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.mb.alps.domain.enums.EmploymentStatus;
import me.mb.alps.domain.exception.DomainException;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "customers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
    @Setter(AccessLevel.NONE)
    private UUID id;

    @Column(unique = true, nullable = false, length = 32)
    private String civilId;

    @Column(nullable = false, length = 255)
    private String fullName;

    @Column(length = 255)
    private String email;

    @Column(length = 32)
    private String phoneNumber;

    @Column(name = "monthly_income", precision = 19, scale = 4)
    private BigDecimal monthlyIncome;

    @Column(name = "credit_score")
    @Setter(AccessLevel.NONE)
    private int creditScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "employment_status", length = 32)
    private EmploymentStatus employmentStatus;

    @Column(name = "age")
    private int age;

    /** Cập nhật điểm tín dụng (từ workflow thưởng/phạt hoặc CIC). */
    public void updateCreditScore(int newScore) {
        if (newScore < 0 || newScore > 1000) {
            throw new DomainException(
                    "Điểm tín dụng phải trong khoảng 0–1000: " + newScore);
        }
        this.creditScore = newScore;
    }
}
