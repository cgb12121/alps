package me.mb.alps.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.mb.alps.domain.enums.RiskDecision;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Result of one Drools scoring run. No status – application holds lifecycle state; this is audit.
 */
@Entity
@Table(name = "risk_assessments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RiskAssessment {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
    @Setter(AccessLevel.NONE)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "application_id", nullable = false)
    private LoanApplication application;

    @Column(name = "risk_score", nullable = false)
    private int riskScore;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private RiskDecision decision;

    /** Audit: why this decision (e.g. "Low income, bad CIC"). */
    @Column(name = "rule_reasons", columnDefinition = "TEXT")
    private String ruleReasons;

    @Column(name = "assessed_at", nullable = false)
    private LocalDateTime assessedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rule_set_version_id")
    private RuleSetVersion ruleSetVersion;
}
