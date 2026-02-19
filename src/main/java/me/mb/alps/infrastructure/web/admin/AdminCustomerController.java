package me.mb.alps.infrastructure.web.admin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.mb.alps.application.dto.request.CreateCustomerRequest;
import me.mb.alps.application.dto.response.ApiResponse;
import me.mb.alps.application.dto.response.CreateCustomerResponse;
import me.mb.alps.application.port.in.admin.CreateCustomerUseCase;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * ADMIN: quản lý khách hàng.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/customers")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Customers", description = "Admin APIs to manage customers")
public class AdminCustomerController {

    private final CreateCustomerUseCase createCustomerUseCase;

    @PostMapping
    @Operation(
            summary = "Create customer",
            description = "Create a new customer record. Initial internal credit score is set according to business rules."
    )
    public ResponseEntity<@NonNull ApiResponse<CreateCustomerResponse>> create(@Valid @RequestBody CreateCustomerRequest request) {
        var command = new CreateCustomerUseCase.CreateCustomerCommand(
                request.civilId(),
                request.fullName(),
                request.email(),
                request.phoneNumber(),
                request.monthlyIncome(),
                request.creditScore(),
                request.employmentStatus(),
                request.age()
        );
        var id = createCustomerUseCase.create(command);
        var body = ApiResponse.success(new CreateCustomerResponse(id), "Customer created");
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }
}
