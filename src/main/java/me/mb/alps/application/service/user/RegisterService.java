package me.mb.alps.application.service.user;

import lombok.RequiredArgsConstructor;
import me.mb.alps.application.port.in.user.RegisterUseCase;
import me.mb.alps.application.port.out.LoadUserPort;
import me.mb.alps.application.port.out.SaveUserPort;
import me.mb.alps.domain.entity.User;
import me.mb.alps.domain.enums.UserRole;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RegisterService implements RegisterUseCase {

    private final LoadUserPort loadUserPort;
    private final SaveUserPort saveUserPort;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UUID register(RegisterCommand command) {
        if (loadUserPort.findByUsername(command.username()).isPresent()) {
            throw new IllegalArgumentException("Username already exists: " + command.username());
        }
        User user = User.builder()
                .username(command.username())
                .passwordHash(passwordEncoder.encode(command.password()))
                .displayName(command.displayName())
                .email(command.email())
                .role(UserRole.CUSTOMER)
                .customerId(command.customerId())
                .active(true)
                .build();
        return saveUserPort.save(user).getId();
    }
}
