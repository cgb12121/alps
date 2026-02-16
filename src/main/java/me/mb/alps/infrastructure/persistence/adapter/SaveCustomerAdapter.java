package me.mb.alps.infrastructure.persistence.adapter;

import lombok.RequiredArgsConstructor;
import me.mb.alps.application.port.out.SaveCustomerPort;
import me.mb.alps.domain.entity.Customer;
import me.mb.alps.infrastructure.persistence.jpa.CustomerJpaRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SaveCustomerAdapter implements SaveCustomerPort {

    private final CustomerJpaRepository jpaRepository;

    @Override
    public Customer save(Customer customer) {
        return jpaRepository.save(customer);
    }
}
