package me.mb.alps.infrastructure.web.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.mb.alps.application.dto.request.SubmitLoanRequest;
import me.mb.alps.application.dto.response.SubmitLoanResponse;
import me.mb.alps.application.port.in.user.SubmitLoanApplicationUseCase;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST adapter for user (khách hàng / người nộp hồ sơ).
 * Cần authentication (JWT); có thể thêm @PreAuthorize("hasRole('USER')") khi đã có role.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user/loan-applications")
public class UserLoanApplicationController {

    private final SubmitLoanApplicationUseCase submitLoanApplicationUseCase;

    @PostMapping
    public ResponseEntity<@NonNull SubmitLoanResponse> submit(@Valid @RequestBody SubmitLoanRequest request) {
        var command = new SubmitLoanApplicationUseCase.SubmitLoanCommand(
                request.customerId(),
                request.productId(),
                request.amount(),
                request.termMonths(),
                request.submittedByUserId()
        );
        var id = submitLoanApplicationUseCase.submit(command);
        return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED)
                .body(new SubmitLoanResponse(id));
    }
}
