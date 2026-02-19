package me.mb.alps.application.service.approver;

import me.mb.alps.application.port.out.LoanApplicationPersistencePort;
import me.mb.alps.domain.entity.Customer;
import me.mb.alps.domain.entity.LoanApplication;
import me.mb.alps.domain.entity.LoanProduct;
import me.mb.alps.domain.enums.LoanStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListPendingApprovalsServiceTest {

    @Mock
    private LoanApplicationPersistencePort persistencePort;

    @InjectMocks
    private ListPendingApprovalsService listPendingApprovalsService;

    @Test
    void list_returnsSummariesForReviewRequired() {
        UUID appId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        var customer = Customer.builder().id(customerId).civilId("c1").fullName("A").build();
        var product = LoanProduct.builder().id(productId).code("P1").name("Vay")
                .minAmount(BigDecimal.ONE).maxAmount(BigDecimal.TEN).minTermMonths(6).maxTermMonths(36)
                .interestRateAnnual(BigDecimal.ZERO).active(true).build();
        var app = LoanApplication.builder()
                .id(appId)
                .customer(customer)
                .product(product)
                .amount(new BigDecimal("10000000"))
                .termMonths(12)
                .status(LoanStatus.REVIEW_REQUIRED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(persistencePort.findByStatus(LoanStatus.REVIEW_REQUIRED)).thenReturn(List.of(app));

        var result = listPendingApprovalsService.list();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().id()).isEqualTo(appId);
        assertThat(result.getFirst().customerId()).isEqualTo(customerId);
        assertThat(result.getFirst().productId()).isEqualTo(productId);
        assertThat(result.getFirst().amount()).isEqualByComparingTo("10000000");
        assertThat(result.getFirst().termMonths()).isEqualTo(12);
        assertThat(result.getFirst().status()).isEqualTo(LoanStatus.REVIEW_REQUIRED);
    }

    @Test
    void list_emptyWhenNoPending() {
        when(persistencePort.findByStatus(LoanStatus.REVIEW_REQUIRED)).thenReturn(List.of());

        var result = listPendingApprovalsService.list();

        assertThat(result).isEmpty();
    }
}
