package me.mb.alps.infrastructure.persistence.jpa;

import me.mb.alps.domain.entity.Customer;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CustomerJpaRepository extends JpaRepository<@NonNull Customer, @NonNull UUID> {
}
