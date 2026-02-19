package me.mb.alps.application.service.admin;

import lombok.RequiredArgsConstructor;
import me.mb.alps.application.port.in.admin.CreateUserUseCase;
import me.mb.alps.application.port.out.SaveUserPort;
import me.mb.alps.domain.entity.User;
import me.mb.alps.domain.enums.UserRole;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreateUserService implements CreateUserUseCase {

    private final SaveUserPort saveUserPort;

    @Override
    public UUID create(CreateUserCommand command) {
        User user = User.builder()
                .username(command.username())
                .displayName(command.displayName())
                .email(command.email())
                .role(UserRole.CUSTOMER)
                .active(command.active())
                .build();
        return saveUserPort.save(user).getId();
    }
}
