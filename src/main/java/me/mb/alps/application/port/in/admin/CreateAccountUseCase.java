package me.mb.alps.application.port.in.admin;

import java.util.UUID;

/**
 * Tạo account: ADMIN được tạo IT, APPROVER; IT được tạo APPROVER.
 * Thông tin caller (role) được truyền qua command (không phụ thuộc SecurityContext trong service).
 */
public interface CreateAccountUseCase {

    UUID create(CreateAccountCommand command);

    record CreateAccountCommand(
            me.mb.alps.domain.enums.UserRole callerRole,
            String username,
            String password,
            String displayName,
            String email,
            me.mb.alps.domain.enums.UserRole role
    ) {}
}
