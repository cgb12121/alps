package me.mb.alps.infrastructure.web.auth;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.mb.alps.infrastructure.security.AlpsUserDetails;
import me.mb.alps.infrastructure.security.JwtTokenProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Login (username + password) → JWT. Không cần auth cho endpoint này.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );
        AlpsUserDetails user = (AlpsUserDetails) auth.getPrincipal();
        List<String> roles = user.roles();
        String token = jwtTokenProvider.createToken(user.getUsername(), roles);
        return ResponseEntity.ok(Map.of(
                "token", token,
                "username", user.getUsername(),
                "userId", user.userId()
        ));
    }

    public record LoginRequest(String username, String password) {}
}
