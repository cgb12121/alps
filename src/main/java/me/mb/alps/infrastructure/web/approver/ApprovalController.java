package me.mb.alps.infrastructure.web.approver;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.mb.alps.application.dto.request.CompleteApprovalRequest;
import me.mb.alps.application.dto.response.LoanApplicationSummaryResponse;
import me.mb.alps.application.port.in.approver.CompleteManualApprovalUseCase;
import me.mb.alps.application.port.in.approver.ListPendingApprovalsUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST adapter cho nhân viên duyệt vay.
 * Cần authentication (JWT) + role APPROVER; dùng @PreAuthorize("hasRole('APPROVER')") khi đã có role.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/approver/loan-applications")
public class ApprovalController {

    private final ListPendingApprovalsUseCase listPendingApprovalsUseCase;
    private final CompleteManualApprovalUseCase completeManualApprovalUseCase;

    /** Danh sách hồ sơ chờ duyệt tay (status = REVIEW_REQUIRED). */
    @GetMapping("/pending-approval")
    public List<LoanApplicationSummaryResponse> listPendingApproval() {
        return listPendingApprovalsUseCase.list();
    }

    /** Duyệt chấp thuận. */
    @PostMapping("/{id}/approve")
    public ResponseEntity<Void> approve(
            @PathVariable UUID id,
            @RequestBody(required = false) CompleteApprovalRequest request) {
        completeManualApprovalUseCase.complete(new CompleteManualApprovalUseCase.CompleteManualApprovalCommand(
                id,
                true,
                request != null ? request.reviewedByUserId() : null
        ));
        return ResponseEntity.noContent().build();
    }

    /** Từ chối. */
    @PostMapping("/{id}/reject")
    public ResponseEntity<Void> reject(
            @PathVariable UUID id,
            @RequestBody(required = false) CompleteApprovalRequest request) {
        completeManualApprovalUseCase.complete(new CompleteManualApprovalUseCase.CompleteManualApprovalCommand(
                id,
                false,
                request != null ? request.reviewedByUserId() : null
        ));
        return ResponseEntity.noContent().build();
    }
}
