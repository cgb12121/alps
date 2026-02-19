package me.mb.alps.infrastructure.web.admin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.mb.alps.application.dto.request.CreateUserRequest;
import me.mb.alps.application.dto.response.ApiResponse;
import me.mb.alps.application.dto.response.CreateUserResponse;
import me.mb.alps.application.port.in.admin.CreateUserUseCase;

import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * ADMIN: quản lý user (tạo user không role/password - legacy). Tạo IT/APPROVER dùng POST /api/accounts.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Users", description = "Admin APIs to manage application users")
public class AdminUserController {

    private final CreateUserUseCase createUserUseCase;

    @PostMapping
    @Operation(
            summary = "Create legacy user",
            description = "Create a user without role/password (legacy path). IT/APPROVER accounts should be created via /api/accounts."
    )
    public ResponseEntity<@NonNull ApiResponse<CreateUserResponse>> create(@Valid @RequestBody CreateUserRequest request) {
        var command = new CreateUserUseCase.CreateUserCommand(
                request.username(),
                request.displayName(),
                request.email(),
                request.active() != null ? request.active() : true
        );
        var id = createUserUseCase.create(command);
        var body = ApiResponse.success(new CreateUserResponse(id), "User created");
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }
}
