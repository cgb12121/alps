package me.mb.alps.infrastructure.web.admin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.mb.alps.application.dto.request.CreateCustomerRequest;
import me.mb.alps.application.dto.response.CreateCustomerResponse;
import me.mb.alps.application.port.in.admin.CreateCustomerUseCase;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST adapter cho admin: quản lý khách hàng.
 * Cần authentication (JWT) + role ADMIN.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/customers")
public class AdminCustomerController {

    private final CreateCustomerUseCase createCustomerUseCase;

    @PostMapping
    public ResponseEntity<@NonNull CreateCustomerResponse> create(@Valid @RequestBody CreateCustomerRequest request) {
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
        return ResponseEntity.status(HttpStatus.CREATED).body(new CreateCustomerResponse(id));
    }
}
