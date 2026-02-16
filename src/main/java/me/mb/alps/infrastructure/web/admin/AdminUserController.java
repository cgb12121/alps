package me.mb.alps.infrastructure.web.admin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.mb.alps.application.dto.request.CreateUserRequest;
import me.mb.alps.application.dto.response.CreateUserResponse;
import me.mb.alps.application.port.in.admin.CreateUserUseCase;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST adapter cho admin: quản lý user.
 * Cần authentication (JWT) + role ADMIN.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final CreateUserUseCase createUserUseCase;

    @PostMapping
    public ResponseEntity<@NonNull CreateUserResponse> create(@Valid @RequestBody CreateUserRequest request) {
        var command = new CreateUserUseCase.CreateUserCommand(
                request.username(),
                request.displayName(),
                request.email(),
                request.active() != null ? request.active() : true
        );
        var id = createUserUseCase.create(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(new CreateUserResponse(id));
    }
}
