package me.mb.alps.infrastructure.security;

import me.mb.alps.application.port.out.LoadUserPort;
import me.mb.alps.domain.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Load user by username for login. Password: chưa lưu trên User thì dùng alps.security.dev-default-password (chỉ dev).
 * Sau này thêm passwordHash vào User và so sánh BCrypt.
 */
@Service
public class AlpsUserDetailsService implements UserDetailsService {

    private final LoadUserPort loadUserPort;
    private final String devDefaultPassword;

    public AlpsUserDetailsService(
            LoadUserPort loadUserPort,
            @Value("${alps.security.dev-default-password:dev}") String devDefaultPassword
    ) {
        this.loadUserPort = loadUserPort;
        this.devDefaultPassword = devDefaultPassword;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = loadUserPort.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        if (!user.isActive()) {
            throw new UsernameNotFoundException("User inactive: " + username);
        }
        // Chưa có password trong DB: dùng dev default. Sau này dùng user.getPasswordHash() với BCrypt.
        String password = devDefaultPassword;
        List<String> roles = List.of("USER"); // TODO: lấy từ user.getRoles() khi có field
        return new AlpsUserDetails(user.getId().toString(), user.getUsername(), password, roles);
    }
}
