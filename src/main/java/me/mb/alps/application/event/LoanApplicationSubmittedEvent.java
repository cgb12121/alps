package me.mb.alps.application.event;

import java.util.UUID;

/**
 * Published after a loan application is persisted (transaction committed).
 * Infrastructure listens to start the Camunda process and update processInstanceKey.
 */
public record LoanApplicationSubmittedEvent(UUID applicationId) {}
