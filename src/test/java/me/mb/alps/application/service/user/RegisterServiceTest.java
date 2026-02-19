package me.mb.alps.application.service.user;

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
class RegisterServiceTest {

    @Mock
    private LoadUserPort loadUserPort;
    @Mock
    private SaveUserPort saveUserPort;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private RegisterService registerService;

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
    void register_createsCustomerUser_andReturnsId() {
        when(loadUserPort.findByUsername("newuser")).thenReturn(Optional.empty());

        var command = new me.mb.alps.application.port.in.user.RegisterUseCase.RegisterCommand(
                "newuser", "pass123", "Display", "a@b.com", null
        );
        UUID id = registerService.register(command);

        assertThat(id).isEqualTo(SAVED_ID);
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(saveUserPort).save(captor.capture());
        User saved = captor.getValue();
        assertThat(saved.getUsername()).isEqualTo("newuser");
        assertThat(saved.getRole()).isEqualTo(UserRole.CUSTOMER);
        assertThat(saved.getPasswordHash()).isEqualTo("encoded");
        assertThat(saved.getCustomerId()).isNull();
    }

    @Test
    void register_withCustomerId_setsCustomerId() {
        when(loadUserPort.findByUsername("u")).thenReturn(Optional.empty());
        UUID customerId = UUID.randomUUID();

        var command = new me.mb.alps.application.port.in.user.RegisterUseCase.RegisterCommand(
                "u", "p", null, null, customerId
        );
        registerService.register(command);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(saveUserPort).save(captor.capture());
        assertThat(captor.getValue().getCustomerId()).isEqualTo(customerId);
    }

    @Test
    void register_usernameExists_throws() {
        when(loadUserPort.findByUsername("existing")).thenReturn(Optional.of(
                User.builder().username("existing").build()
        ));

        var command = new me.mb.alps.application.port.in.user.RegisterUseCase.RegisterCommand(
                "existing", "p", null, null, null
        );
        assertThatThrownBy(() -> registerService.register(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Username already exists");
    }
}
