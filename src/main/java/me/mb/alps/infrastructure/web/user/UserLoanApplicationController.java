package me.mb.alps.infrastructure.web.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.mb.alps.application.dto.request.SubmitLoanRequest;
import me.mb.alps.application.dto.response.ApiResponse;
import me.mb.alps.application.dto.response.LoanApplicationSummaryResponse;
import me.mb.alps.application.dto.response.PaymentScheduleItemResponse;
import me.mb.alps.application.dto.response.RepaymentScheduleItemResponse;
import me.mb.alps.application.dto.response.SubmitLoanResponse;
import me.mb.alps.application.dto.request.MakePaymentRequest;
import me.mb.alps.application.port.in.user.GetLoanScheduleUseCase;
import me.mb.alps.application.port.in.user.GetMyLoanApplicationsUseCase;
import me.mb.alps.application.port.in.user.GetRepaymentScheduleUseCase;
import me.mb.alps.application.port.in.user.MakePaymentUseCase;
import me.mb.alps.application.port.in.user.SubmitLoanApplicationUseCase;
import me.mb.alps.infrastructure.security.AlpsUserDetails;
import org.jspecify.annotations.NonNull;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * CUSTOMER: nộp hồ sơ vay và xem loan của mình (theo customerId gắn với user).
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user/loan-applications")
@PreAuthorize("hasRole('CUSTOMER')")
@Tag(name = "User Loans", description = "APIs for customers to submit and manage their loans")
public class UserLoanApplicationController {

    private final SubmitLoanApplicationUseCase submitLoanApplicationUseCase;
    private final GetMyLoanApplicationsUseCase getMyLoanApplicationsUseCase;
    private final GetLoanScheduleUseCase getLoanScheduleUseCase;
    private final GetRepaymentScheduleUseCase getRepaymentScheduleUseCase;
    private final MakePaymentUseCase makePaymentUseCase;

    @PostMapping
    @Operation(
            summary = "Submit loan application",
            description = "Customer submits a new loan application which triggers Camunda + Drools scoring."
    )
    public ResponseEntity<@NonNull ApiResponse<SubmitLoanResponse>> submit(@Valid @RequestBody SubmitLoanRequest request) {
        var command = new SubmitLoanApplicationUseCase.SubmitLoanCommand(
                request.customerId(),
                request.productId(),
                request.amount(),
                request.termMonths(),
                request.submittedByUserId()
        );
        var id = submitLoanApplicationUseCase.submit(command);
        var body = ApiResponse.success(new SubmitLoanResponse(id), "Loan application submitted");
        return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED).body(body);
    }

    /** Danh sách loan của CUSTOMER (theo user.customerId). */
    @GetMapping("/me")
    @Operation(
            summary = "List my loans",
            description = "Return all loan applications of the authenticated CUSTOMER (based on mapped customerId)."
    )
    public ResponseEntity<@NonNull ApiResponse<List<LoanApplicationSummaryResponse>>> listMyLoans(
            @AuthenticationPrincipal AlpsUserDetails currentUser
    ) {
        List<LoanApplicationSummaryResponse> loans =
                getMyLoanApplicationsUseCase.listMyLoans(UUID.fromString(currentUser.userId()), currentUser.getRole());
        return ResponseEntity.ok(ApiResponse.success(loans));
    }

    /** Lịch trả nợ đã được generate (pre-computed) từ DB. */
    @GetMapping("/{id}/repayment-schedule")
    @Operation(
            summary = "Get repayment schedule",
            description = "Get pre-computed repayment schedule for an approved loan (saved in DB)."
    )
    public ResponseEntity<@NonNull ApiResponse<List<RepaymentScheduleItemResponse>>> getRepaymentSchedule(
            @PathVariable UUID id,
            @AuthenticationPrincipal AlpsUserDetails currentUser
    ) {
        List<RepaymentScheduleItemResponse> schedule =
                getRepaymentScheduleUseCase.getSchedule(id, UUID.fromString(currentUser.userId()), currentUser.getRole());
        return ResponseEntity.ok(ApiResponse.success(schedule));
    }

    /** Tính toán lịch trả `nợ (chưa lưu DB) - dùng để preview trước khi approve. firstPaymentDate mặc định là ngày hiện tại + 1 tháng. */
    @GetMapping("/{id}/schedule-preview")
    @Operation(
            summary = "Preview repayment schedule",
            description = "Calculate repayment schedule on-the-fly without persisting, useful for preview before approval."
    )
    public ResponseEntity<@NonNull ApiResponse<List<PaymentScheduleItemResponse>>> getSchedulePreview(
            @PathVariable UUID id,
            @AuthenticationPrincipal AlpsUserDetails currentUser,
            @RequestParam(required = false) LocalDate firstPaymentDate
    ) {
        LocalDate firstDate = firstPaymentDate != null
                ? firstPaymentDate
                : LocalDate.now().plusMonths(1);
        List<PaymentScheduleItemResponse> schedule = getLoanScheduleUseCase
                .getSchedule(id, firstDate, UUID.fromString(currentUser.userId()), currentUser.getRole())
                .stream()
                .map(item -> new PaymentScheduleItemResponse(
                        item.period(),
                        item.paymentDate(),
                        item.principalAmount(),
                        item.interestAmount(),
                        item.totalPayment(),
                        item.remainingPrincipal()
                ))
                .toList();
        return ResponseEntity.ok(ApiResponse.success(schedule));
    }

    /** Customer trả loan (có thể trả trước hạn hoặc trả một phần). */
    @PostMapping("/{id}/payments")
    @Operation(
            summary = "Make a payment",
            description = "Customer makes a payment for a specific loan installment (or multiple installments). Supports early/partial payments."
    )
    public ResponseEntity<@NonNull ApiResponse<MakePaymentUseCase.PaymentResult>> makePayment(
            @PathVariable UUID id,
            @AuthenticationPrincipal AlpsUserDetails currentUser,
            @Valid @RequestBody MakePaymentRequest request) {
        var command = new MakePaymentUseCase.MakePaymentCommand(
                id,
                request.repaymentScheduleId(),
                request.amount(),
                UUID.fromString(currentUser.userId())
        );
        MakePaymentUseCase.PaymentResult result = makePaymentUseCase.makePayment(command);
        return ResponseEntity.ok(ApiResponse.success(result, "Payment processed"));
    }
}
