package me.mb.alps.infrastructure.security;

import me.mb.alps.application.port.out.LoadUserPort;
import me.mb.alps.domain.entity.User;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Load user by username for login. Password: từ User.passwordHash nếu có; nếu không dùng dev-default (encode để matches).
 */
@Service
public class AlpsUserDetailsService implements UserDetailsService {

    private final LoadUserPort loadUserPort;
    private final PasswordEncoder passwordEncoder;
    private final String devDefaultPassword;

    public AlpsUserDetailsService(
            LoadUserPort loadUserPort,
            PasswordEncoder passwordEncoder,
            @Value("${alps.security.dev-default-password:dev}") String devDefaultPassword
    ) {
        this.loadUserPort = loadUserPort;
        this.passwordEncoder = passwordEncoder;
        this.devDefaultPassword = devDefaultPassword;
    }

    @Override
    public @NonNull UserDetails loadUserByUsername(@NonNull String username) throws UsernameNotFoundException {
        User user = loadUserPort.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        if (!user.isActive()) {
            throw new UsernameNotFoundException("User inactive: " + username);
        }
        String password = user.getPasswordHash() != null && !user.getPasswordHash().isBlank()
                ? user.getPasswordHash()
                : passwordEncoder.encode(devDefaultPassword);

        List<String> roles = List.of(user.getRole().name());
        return new AlpsUserDetails(
                user.getId().toString(),
                user.getUsername(),
                password,
                roles
        );
    }
}
