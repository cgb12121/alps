package me.mb.alps.application.port.out;

import me.mb.alps.domain.entity.User;

import java.util.Optional;
import java.util.UUID;

/**
 * Outbound port: load user by id or username. Implemented by infrastructure.persistence.
 */
public interface LoadUserPort {
    Optional<User> findById(UUID id);

    /** For login / UserDetailsService. */
    Optional<User> findByUsername(String username);
}
