package me.mb.alps.application.event;

import me.mb.alps.domain.enums.LoanStatus;

import java.util.UUID;

/**
 * Published when a loan application reaches a final decision (APPROVED or REJECTED).
 * Triggers notification (email/Slack).
 */
public record LoanApplicationDecidedEvent(UUID applicationId, LoanStatus newStatus) {}
