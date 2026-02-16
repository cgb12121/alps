package me.mb.alps.infrastructure.persistence.jpa;

import me.mb.alps.domain.entity.User;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserJpaRepository extends JpaRepository<@NonNull User, @NonNull UUID> {

    java.util.Optional<User> findByUsername(String username);
}
