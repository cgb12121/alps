package me.mb.alps.application.port.in.admin;

import java.util.UUID;

public interface CreateUserUseCase {

    UUID create(CreateUserCommand command);

    record CreateUserCommand(String username, String displayName, String email, boolean active) {}
}
