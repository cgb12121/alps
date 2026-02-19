package me.mb.alps.application.service.admin;

import me.mb.alps.application.port.out.LoadUserPort;
import me.mb.alps.application.port.out.SaveUserPort;
import me.mb.alps.domain.entity.User;
import me.mb.alps.domain.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CreateAccountServiceTest {

    @Mock
    private LoadUserPort loadUserPort;
    @Mock
    private SaveUserPort saveUserPort;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private CreateAccountService createAccountService;

    private static final UUID CALLER_ID = UUID.randomUUID();
    private static final UUID SAVED_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        when(passwordEncoder.encode(any())).thenReturn("encoded");
        when(saveUserPort.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            return new User(SAVED_ID, u.getUsername(), u.getDisplayName(), u.getEmail(),
                    u.getPasswordHash(), u.getRole(), u.getCustomerId(), u.isActive());
        });
    }

    @Test
    void create_adminCreatesApprover_success() {
        when(loadUserPort.findByUsername("approver1")).thenReturn(Optional.empty());

        var command = new me.mb.alps.application.port.in.admin.CreateAccountUseCase.CreateAccountCommand(
                UserRole.ADMIN,
                "approver1", "pass", "Approver", "a@b.com", UserRole.APPROVER
        );
        UUID id = createAccountService.create(command);

        assertThat(id).isEqualTo(SAVED_ID);
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(saveUserPort).save(captor.capture());
        assertThat(captor.getValue().getUsername()).isEqualTo("approver1");
        assertThat(captor.getValue().getRole()).isEqualTo(UserRole.APPROVER);
    }

    @Test
    void create_itCreatesApprover_success() {
        when(loadUserPort.findByUsername("appr")).thenReturn(Optional.empty());

        var command = new me.mb.alps.application.port.in.admin.CreateAccountUseCase.CreateAccountCommand(
                UserRole.IT,
                "appr", "p", null, null, UserRole.APPROVER
        );
        createAccountService.create(command);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(saveUserPort).save(captor.capture());
        assertThat(captor.getValue().getRole()).isEqualTo(UserRole.APPROVER);
    }

    @Test
    void create_adminCannotCreateCustomer_throws() {
        var command = new me.mb.alps.application.port.in.admin.CreateAccountUseCase.CreateAccountCommand(
                UserRole.ADMIN,
                "u", "p", null, null, UserRole.CUSTOMER
        );
        assertThatThrownBy(() -> createAccountService.create(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cannot create account with role");
    }

    @Test
    void create_itCannotCreateIT_throws() {
        var command = new me.mb.alps.application.port.in.admin.CreateAccountUseCase.CreateAccountCommand(
                UserRole.IT,
                "u", "p", null, null, UserRole.IT
        );
        assertThatThrownBy(() -> createAccountService.create(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot create role");
    }

    @Test
    void create_usernameExists_throws() {
        var command = new me.mb.alps.application.port.in.admin.CreateAccountUseCase.CreateAccountCommand(
                UserRole.ADMIN, "dup", "p", null, null, UserRole.APPROVER
        );
        assertThatThrownBy(() -> createAccountService.create(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Username already exists");
    }
}
