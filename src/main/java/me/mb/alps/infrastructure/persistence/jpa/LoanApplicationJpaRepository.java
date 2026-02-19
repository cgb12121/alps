package me.mb.alps.infrastructure.persistence.jpa;

import me.mb.alps.domain.entity.LoanApplication;
import me.mb.alps.domain.enums.LoanStatus;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository. Used only by persistence adapter; application layer uses port interface.
 */
@Repository
public interface LoanApplicationJpaRepository extends JpaRepository<@NonNull LoanApplication, @NonNull UUID> {
    List<LoanApplication> findByStatusOrderByCreatedAtDesc(@NonNull LoanStatus status);
    List<LoanApplication> findByCustomer_IdOrderByCreatedAtDesc(@NonNull UUID customerId);
    
    @Query("SELECT la FROM LoanApplication la WHERE " +
           "(:status IS NULL OR la.status = :status) AND " +
           "(:reviewedById IS NULL OR la.reviewedBy.id = :reviewedById) AND " +
           "(:fromDate IS NULL OR la.createdAt >= :fromDate) AND " +
           "(:toDate IS NULL OR la.createdAt <= :toDate) " +
           "ORDER BY la.createdAt DESC")
    List<LoanApplication> findAllWithFilters(
            @Param("status") LoanStatus status,
            @Param("reviewedById") UUID reviewedById,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate
    );
}
