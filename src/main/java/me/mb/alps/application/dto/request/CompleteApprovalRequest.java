package me.mb.alps.application.dto.request;

import java.util.UUID;

/**
 * Request body for approve/reject manual approval. reviewedByUserId optional.
 */
public record CompleteApprovalRequest(UUID reviewedByUserId) {}
