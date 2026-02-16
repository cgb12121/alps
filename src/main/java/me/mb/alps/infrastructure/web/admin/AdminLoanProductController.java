package me.mb.alps.infrastructure.web.admin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.mb.alps.application.dto.request.CreateLoanProductRequest;
import me.mb.alps.application.dto.response.CreateLoanProductResponse;
import me.mb.alps.application.port.in.admin.CreateLoanProductUseCase;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST adapter cho admin: quản lý sản phẩm vay.
 * Cần authentication (JWT) + role ADMIN.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/loan-products")
public class AdminLoanProductController {

    private final CreateLoanProductUseCase createLoanProductUseCase;

    @PostMapping
    public ResponseEntity<@NonNull CreateLoanProductResponse> create(@Valid @RequestBody CreateLoanProductRequest request) {
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
        return ResponseEntity.status(HttpStatus.CREATED).body(new CreateLoanProductResponse(id));
    }
}
