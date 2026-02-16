package me.mb.alps.infrastructure.persistence.adapter;

import lombok.RequiredArgsConstructor;
import me.mb.alps.application.port.out.LoadCustomerPort;
import me.mb.alps.domain.entity.Customer;
import me.mb.alps.infrastructure.persistence.jpa.CustomerJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class LoadCustomerAdapter implements LoadCustomerPort {

    private final CustomerJpaRepository jpaRepository;

    @Override
    public Optional<Customer> findById(UUID id) {
        return jpaRepository.findById(id);
    }
}
