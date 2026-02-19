package me.mb.alps.application.port.out;

import java.util.UUID;

/**
 * Outbound port: update customer credit score (CIC nội bộ).
 */
public interface UpdateCustomerCreditScorePort {
    void updateCreditScore(UUID customerId, int newScore);
    int getCreditScore(UUID customerId);
}
