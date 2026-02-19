package me.mb.alps.application.service.admin;

import lombok.RequiredArgsConstructor;
import me.mb.alps.application.port.in.admin.CreateAccountUseCase;
import me.mb.alps.application.port.out.LoadUserPort;
import me.mb.alps.application.port.out.SaveUserPort;
import me.mb.alps.domain.entity.User;
import me.mb.alps.domain.enums.UserRole;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreateAccountService implements CreateAccountUseCase {

    private static final Set<UserRole> ADMIN_CAN_CREATE = Set.of(UserRole.IT, UserRole.APPROVER);
    private static final Set<UserRole> IT_CAN_CREATE = Set.of(UserRole.APPROVER);

    private final LoadUserPort loadUserPort;
    private final SaveUserPort saveUserPort;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UUID create(CreateAccountCommand command) {
        UserRole callerRole = command.callerRole();
        UserRole requestedRole = command.role();
        if (requestedRole == UserRole.CUSTOMER || requestedRole == UserRole.ADMIN) {
            throw new IllegalArgumentException("Cannot create account with role " + requestedRole);
        }
        Set<UserRole> allowed = switch (callerRole) {
            case ADMIN -> ADMIN_CAN_CREATE;
            case IT -> IT_CAN_CREATE;
            default -> Set.of();
        };
        if (!allowed.contains(requestedRole)) {
            throw new IllegalArgumentException("Caller role " + callerRole + " cannot create role " + requestedRole);
        }
        if (loadUserPort.findByUsername(command.username()).isPresent()) {
            throw new IllegalArgumentException("Username already exists: " + command.username());
        }
        User user = User.builder()
                .username(command.username())
                .passwordHash(passwordEncoder.encode(command.password()))
                .displayName(command.displayName())
                .email(command.email())
                .role(requestedRole)
                .active(true)
                .build();
        return saveUserPort.save(user).getId();
    }
}

