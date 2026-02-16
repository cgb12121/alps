package me.mb.alps.application.port.out;

import me.mb.alps.domain.entity.User;

/**
 * Outbound port: persist a user. Returns the saved entity (with id set).
 */
public interface SaveUserPort {

    User save(User user);
}
