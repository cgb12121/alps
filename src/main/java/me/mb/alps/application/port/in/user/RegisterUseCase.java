package me.mb.alps.application.port.in.user;

import java.util.UUID;

/**
 * Đăng ký tài khoản CUSTOMER. Optional: map tới customer có sẵn (customerId).
 */
public interface RegisterUseCase {

    UUID register(RegisterCommand command);

    record RegisterCommand(
            String username,
            String password,
            String displayName,
            String email,
            UUID customerId
    ) {}
}
