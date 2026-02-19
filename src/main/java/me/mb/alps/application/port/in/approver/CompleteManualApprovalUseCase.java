package me.mb.alps.application.port.in.approver;

import java.util.UUID;

/**
 * Inbound port: complete manual approval for an application in REVIEW_REQUIRED.
 * Publishes message to Zeebe, updates application status and reviewedBy/reviewedAt.
 */
public interface CompleteManualApprovalUseCase {
    void complete(CompleteManualApprovalCommand command);

    record CompleteManualApprovalCommand(
            UUID applicationId,
            boolean approved,
            UUID reviewedByUserId,
            String comment  // Optional comment for approval history
    ) {}
}
