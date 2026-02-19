package me.mb.alps.infrastructure.web.admin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.mb.alps.application.dto.request.CreateAccountRequest;
import me.mb.alps.application.dto.response.ApiResponse;
import me.mb.alps.application.port.in.admin.CreateAccountUseCase;
import me.mb.alps.infrastructure.security.AlpsUserDetails;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/**
 * ADMIN tạo IT/APPROVER; IT tạo APPROVER. Mỗi account 1 role.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/accounts")
@PreAuthorize("hasAnyRole('ADMIN','IT')")
@Tag(name = "Accounts", description = "APIs to create staff accounts (ADMIN/IT/APPROVER)")
public class AccountController {

    private final CreateAccountUseCase createAccountUseCase;

    @PostMapping
    @Operation(
            summary = "Create staff account",
            description = "ADMIN creates IT/APPROVER; IT can create APPROVER. Each account has exactly one role."
    )
    public ResponseEntity<@NonNull ApiResponse<Map<String, Object>>> create(
            @AuthenticationPrincipal AlpsUserDetails currentUser,
            @Valid @RequestBody CreateAccountRequest request
    ) {
        var command = new CreateAccountUseCase.CreateAccountCommand(
                currentUser.getRole(),
                request.username(),
                request.password(),
                request.displayName(),
                request.email(),
                request.role()
        );
        UUID id = createAccountUseCase.create(command);
        Map<String, Object> payload = Map.of(
                "userId", id.toString(),
                "username", request.username(),
                "role", request.role().name()
        );
        var body = ApiResponse.success(payload, "Account created");
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }
}
