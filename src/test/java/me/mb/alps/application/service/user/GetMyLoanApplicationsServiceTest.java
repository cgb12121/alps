package me.mb.alps.application.service.user;

import me.mb.alps.application.port.out.LoadUserPort;
import me.mb.alps.application.port.out.LoanApplicationPersistencePort;
import me.mb.alps.domain.entity.Customer;
import me.mb.alps.domain.entity.LoanApplication;
import me.mb.alps.domain.entity.LoanProduct;
import me.mb.alps.domain.entity.User;
import me.mb.alps.domain.enums.LoanStatus;
import me.mb.alps.domain.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetMyLoanApplicationsServiceTest {

    @Mock
    private LoadUserPort loadUserPort;
    @Mock
    private LoanApplicationPersistencePort persistencePort;

    @InjectMocks
    private GetMyLoanApplicationsService getMyLoanApplicationsService;

    @Test
    void listMyLoans_returnsLoansForCustomerId() {
        UUID userId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        var user = new User(userId, "u", null, null, null, UserRole.CUSTOMER, customerId, true);
        when(loadUserPort.findById(userId)).thenReturn(Optional.of(user));

        var customer = Customer.builder().id(customerId).civilId("c1").fullName("A").build();
        var product = LoanProduct.builder().id(UUID.randomUUID()).code("P1").name("Vay")
                .minAmount(BigDecimal.ONE).maxAmount(BigDecimal.TEN).minTermMonths(6).maxTermMonths(36)
                .interestRateAnnual(BigDecimal.ZERO).active(true).build();
        var app = LoanApplication.builder()
                .id(UUID.randomUUID())
                .customer(customer)
                .product(product)
                .amount(BigDecimal.TEN)
                .termMonths(12)
                .status(LoanStatus.SUBMITTED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        when(persistencePort.findByCustomerIdOrderByCreatedAtDesc(customerId)).thenReturn(List.of(app));

        var result = getMyLoanApplicationsService.listMyLoans(userId, UserRole.CUSTOMER);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().customerId()).isEqualTo(customerId);
    }

    @Test
    void listMyLoans_userHasNoCustomerId_returnsEmpty() {
        UUID userId = UUID.randomUUID();
        var user = new User(userId, "u", null, null, null, UserRole.CUSTOMER, null, true);
        when(loadUserPort.findById(userId)).thenReturn(Optional.of(user));

        var result = getMyLoanApplicationsService.listMyLoans(userId, UserRole.CUSTOMER);

        assertThat(result).isEmpty();
    }

    @Test
    void listMyLoans_notCustomerRole_throws() {
        UUID userId = UUID.randomUUID();
        assertThatThrownBy(() -> getMyLoanApplicationsService.listMyLoans(userId, UserRole.APPROVER))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Only CUSTOMER");
    }
}
