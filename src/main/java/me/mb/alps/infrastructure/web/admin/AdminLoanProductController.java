package me.mb.alps.infrastructure.web.admin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.mb.alps.application.dto.request.CreateLoanProductRequest;
import me.mb.alps.application.dto.response.ApiResponse;
import me.mb.alps.application.dto.response.CreateLoanProductResponse;
import me.mb.alps.application.port.in.admin.CreateLoanProductUseCase;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * ADMIN: quản lý sản phẩm vay.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/loan-products")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Loan Products", description = "Admin APIs to manage loan products (plans)")
public class AdminLoanProductController {

    private final CreateLoanProductUseCase createLoanProductUseCase;

    @PostMapping
    @Operation(
            summary = "Create loan product",
            description = "Create a new loan product / plan with min/max amount, term, and base interest rate."
    )
    public ResponseEntity<@NonNull ApiResponse<CreateLoanProductResponse>> create(@Valid @RequestBody CreateLoanProductRequest request) {
        var command = new CreateLoanProductUseCase.CreateLoanProductCommand(
                request.code(),
                request.name(),
                request.minAmount(),
                request.maxAmount(),
                request.minTermMonths(),
                request.maxTermMonths(),
                request.interestRateAnnual(),
                request.active() != null ? request.active() : true
        );
        var id = createLoanProductUseCase.create(command);
        var body = ApiResponse.success(new CreateLoanProductResponse(id), "Loan product created");
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }
}
