package me.mb.alps.application.port.out;

import me.mb.alps.domain.entity.LoanApplication;
import me.mb.alps.domain.enums.LoanStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Outbound port: persist loan application. Implemented by infrastructure.persistence adapter.
 */
public interface LoanApplicationPersistencePort {
    LoanApplication save(LoanApplication application);
    Optional<LoanApplication> findById(UUID id);
    List<LoanApplication> findByStatus(LoanStatus status);
    List<LoanApplication> findByCustomerIdOrderByCreatedAtDesc(UUID customerId);
    
    /**
     * Tìm loans với filters: status, reviewedBy, date range.
     * Nếu filter null thì bỏ qua filter đó.
     */
    List<LoanApplication> findAllWithFilters(LoanStatus status, UUID reviewedById, 
                                             LocalDateTime fromDate, LocalDateTime toDate);
}
