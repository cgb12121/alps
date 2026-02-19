package me.mb.alps.infrastructure.web.approver;

import lombok.RequiredArgsConstructor;
import me.mb.alps.application.dto.request.CompleteApprovalRequest;
import me.mb.alps.application.dto.response.ApiResponse;
import me.mb.alps.application.dto.response.ApprovalHistoryResponse;
import me.mb.alps.application.dto.response.LoanApplicationSummaryResponse;
import me.mb.alps.application.port.in.approver.CompleteManualApprovalUseCase;
import me.mb.alps.application.port.in.approver.GetApprovalHistoryUseCase;
import me.mb.alps.application.port.in.approver.ListPendingApprovalsUseCase;
import me.mb.alps.infrastructure.security.AlpsUserDetails;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ResponseEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * APPROVER: chỉ lấy danh sách chờ duyệt và duyệt/từ chối.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/approver/loan-applications")
@PreAuthorize("hasRole('APPROVER')")
@Tag(name = "Approver", description = "APIs for manual reviewers to list and approve/reject loans")
public class ApprovalController {

    private final ListPendingApprovalsUseCase listPendingApprovalsUseCase;
    private final CompleteManualApprovalUseCase completeManualApprovalUseCase;
    private final GetApprovalHistoryUseCase getApprovalHistoryUseCase;

    /** Danh sách hồ sơ chờ duyệt tay (status = REVIEW_REQUIRED). */
    @GetMapping("/pending-approval")
    @Operation(
            summary = "List pending approvals",
            description = "Return all loan applications that require manual review (status = REVIEW_REQUIRED)."
    )
    public ResponseEntity<@NonNull ApiResponse<List<LoanApplicationSummaryResponse>>> listPendingApproval() {
        List<LoanApplicationSummaryResponse> list = listPendingApprovalsUseCase.list();
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    /** Duyệt chấp thuận. */
    @PostMapping("/{id}/approve")
    @Operation(
            summary = "Approve loan manually",
            description = "Approve a loan application manually. Also sends message to Camunda process."
    )
    public ResponseEntity<@NonNull ApiResponse<Void>> approve(
            @PathVariable UUID id,
            @AuthenticationPrincipal AlpsUserDetails currentUser,
            @RequestBody(required = false) CompleteApprovalRequest request) {
        UUID approverId = UUID.fromString(currentUser.userId());
        
        completeManualApprovalUseCase.complete(new CompleteManualApprovalUseCase.CompleteManualApprovalCommand(
                id,
                true,
                approverId,
                request != null ? request.comment() : null
        ));
        return ResponseEntity.ok(ApiResponse.success(null, "Approved"));
    }

    /** Từ chối. */
    @PostMapping("/{id}/reject")
    @Operation(
            summary = "Reject loan manually",
            description = "Reject a loan application manually. Also sends message to Camunda process."
    )
    public ResponseEntity<@NonNull ApiResponse<Void>> reject(
            @PathVariable UUID id,
            @AuthenticationPrincipal AlpsUserDetails currentUser,
            @RequestBody(required = false) CompleteApprovalRequest request) {
        UUID approverId = UUID.fromString(currentUser.userId());
        
        completeManualApprovalUseCase.complete(new CompleteManualApprovalUseCase.CompleteManualApprovalCommand(
                id,
                false,
                approverId,
                request != null ? request.comment() : null
        ));
        return ResponseEntity.ok(ApiResponse.success(null, "Rejected"));
    }

    /** Xem loan của customer (ghi log). */
    @GetMapping("/{id}")
    @Operation(
            summary = "View loan (approver)",
            description = "View details of a loan from the approver's perspective. Currently returns placeholder."
    )
    public ResponseEntity<@NonNull ApiResponse<LoanApplicationSummaryResponse>> viewLoan(
            @PathVariable UUID id,
            @AuthenticationPrincipal AlpsUserDetails currentUser
    ) {
        // UUID approverId = UUID.fromString(currentUser.userId());

        // TODO: Return full loan details, not just summary. For now, return null data with message.
        return ResponseEntity.ok(ApiResponse.success(null, "View loan not implemented yet"));
    }

    /** Lịch sử duyệt của một loan. */
    @GetMapping("/{id}/approval-history")
    @Operation(
            summary = "Get approval history",
            description = "Return all manual approval history entries for a given loan."
    )
    public ResponseEntity<@NonNull ApiResponse<List<ApprovalHistoryResponse>>> getApprovalHistory(@PathVariable UUID id) {
        List<ApprovalHistoryResponse> history = getApprovalHistoryUseCase.getHistory(id);
        return ResponseEntity.ok(ApiResponse.success(history));
    }
}
