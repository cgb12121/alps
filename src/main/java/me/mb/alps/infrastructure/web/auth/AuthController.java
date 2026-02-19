package me.mb.alps.infrastructure.web.auth;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.mb.alps.application.dto.request.RegisterRequest;
import me.mb.alps.application.dto.response.ApiResponse;
import me.mb.alps.application.port.in.user.RegisterUseCase;
import me.mb.alps.infrastructure.security.AlpsUserDetails;
import me.mb.alps.infrastructure.security.JwtTokenProvider;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Đăng ký, đăng nhập. Không cần auth cho các endpoint này.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Authentication APIs: register and login (JWT)")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final RegisterUseCase registerUseCase;

    @PostMapping("/register")
    @Operation(
            summary = "Register new user",
            description = "Public endpoint to register a new user. Returns basic info; use /api/auth/login to get JWT."
    )
    public ResponseEntity<@NonNull ApiResponse<Map<String, Object>>> register(@Valid @RequestBody RegisterRequest request) {
        var command = new RegisterUseCase.RegisterCommand(
                request.username(),
                request.password(),
                request.displayName(),
                request.email(),
                request.customerId()
        );
        UUID id = registerUseCase.register(command);
        Map<String, Object> payload = Map.of(
                "userId", id.toString(),
                "username", request.username(),
                "message", "Registered. Use POST /api/auth/login to get token."
        );
        var body = ApiResponse.success(payload, "Registered");
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @PostMapping("/login")
    @Operation(
            summary = "Login",
            description = "Authenticate with username/password and receive a JWT Bearer token."
    )
    public ResponseEntity<@NonNull ApiResponse<Map<String, Object>>> login(@Valid @RequestBody LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );
        AlpsUserDetails user = (AlpsUserDetails) auth.getPrincipal();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<String> roles = user.roles();
        String token = jwtTokenProvider.createToken(user.getUsername(), user.userId(), roles);
        Map<String, Object> payload = Map.of(
                "token", token,
                "username", user.getUsername(),
                "userId", user.userId(),
                "role", user.getRole().name()
        );
        return ResponseEntity.ok(ApiResponse.success(payload, "Login successful"));
    }

    public record LoginRequest(String username, String password) {}
}
