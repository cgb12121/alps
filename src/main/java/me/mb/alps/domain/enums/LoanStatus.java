package me.mb.alps.domain.enums;

/**
 * Lifecycle status of a loan application. Align with Camunda process/task names when possible.
 */
public enum LoanStatus {
    DRAFT,
    SUBMITTED,
    SCORING,
    REVIEW_REQUIRED,
    APPROVED,
    REJECTED,
    WITHDRAWN,
    DISBURSED
}
