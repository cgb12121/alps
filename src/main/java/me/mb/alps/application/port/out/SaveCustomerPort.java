package me.mb.alps.application.port.out;

import me.mb.alps.domain.entity.Customer;

/**
 * Outbound port: persist a customer. Returns the saved entity (with id set).
 */
public interface SaveCustomerPort {

    Customer save(Customer customer);
}
