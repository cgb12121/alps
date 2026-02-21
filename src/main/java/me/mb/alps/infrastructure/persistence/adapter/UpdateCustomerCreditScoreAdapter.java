package me.mb.alps.infrastructure.persistence.adapter;

import lombok.RequiredArgsConstructor;
import me.mb.alps.application.port.out.LoadCustomerPort;
import me.mb.alps.application.port.out.SaveCustomerPort;
import me.mb.alps.application.port.out.UpdateCustomerCreditScorePort;
import me.mb.alps.domain.entity.Customer;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UpdateCustomerCreditScoreAdapter implements UpdateCustomerCreditScorePort {

    private final LoadCustomerPort loadCustomerPort;
    private final SaveCustomerPort saveCustomerPort;

    @Override
    public void updateCreditScore(UUID customerId, int newScore) {
        Customer customer = loadCustomerPort.findById(customerId)
                .orElseThrow(() -> new IllegalStateException("Customer not found: " + customerId));
        customer.updateCreditScore(newScore);
        saveCustomerPort.save(customer);
    }

    @Override
    public int getCreditScore(UUID customerId) {
        Customer customer = loadCustomerPort.findById(customerId)
                .orElseThrow(() -> new IllegalStateException("Customer not found: " + customerId));
        return customer.getCreditScore();
    }
}
