package me.mb.alps.domain.enums;

/**
 * Outcome from Drools risk engine. Stored on RiskAssessment (no status on RiskAssessment).
 */
public enum RiskDecision {
    AUTO_APPROVE,
    AUTO_REJECT,
    MANUAL_CHECK
}
