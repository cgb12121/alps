package me.mb.alps.application.service.admin;

import me.mb.alps.application.port.out.SaveUserPort;
import me.mb.alps.domain.entity.User;
import me.mb.alps.domain.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateUserServiceTest {

    @Mock
    private SaveUserPort saveUserPort;

    @InjectMocks
    private CreateUserService createUserService;

    private static final UUID SAVED_ID = UUID.randomUUID();

    @Test
    void create_savesUserWithRoleCustomerAndReturnsId() {
        when(saveUserPort.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            return new User(SAVED_ID, u.getUsername(), u.getDisplayName(), u.getEmail(),
                    u.getPasswordHash(), u.getRole(), u.getCustomerId(), u.isActive());
        });

        var command = new me.mb.alps.application.port.in.admin.CreateUserUseCase.CreateUserCommand(
                "u1", "Display", "u@b.com", true
        );
        UUID id = createUserService.create(command);

        assertThat(id).isEqualTo(SAVED_ID);
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(saveUserPort).save(captor.capture());
        User saved = captor.getValue();
        assertThat(saved.getUsername()).isEqualTo("u1");
        assertThat(saved.getRole()).isEqualTo(UserRole.CUSTOMER);
        assertThat(saved.isActive()).isTrue();
    }
}
