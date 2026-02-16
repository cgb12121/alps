package me.mb.alps.application.port.out;

import me.mb.alps.domain.entity.Customer;

import java.util.Optional;
import java.util.UUID;

/**
 * Outbound port: load customer by id. Implemented by infrastructure.persistence.
 */
public interface LoadCustomerPort {
    Optional<Customer> findById(UUID id);
}
