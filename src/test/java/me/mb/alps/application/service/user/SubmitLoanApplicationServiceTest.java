package me.mb.alps.application.service.user;

import me.mb.alps.application.exception.NotFoundException;
import me.mb.alps.application.port.out.LoadCustomerPort;
import me.mb.alps.application.port.out.LoadLoanProductPort;
import me.mb.alps.application.port.out.LoadUserPort;
import me.mb.alps.application.port.out.LoanApplicationPersistencePort;
import me.mb.alps.domain.entity.Customer;
import me.mb.alps.domain.entity.LoanApplication;
import me.mb.alps.domain.entity.LoanProduct;
import me.mb.alps.domain.enums.LoanStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SubmitLoanApplicationServiceTest {

    @Mock
    private LoadCustomerPort loadCustomerPort;
    @Mock
    private LoadLoanProductPort loadLoanProductPort;
    @Mock
    private LoadUserPort loadUserPort;
    @Mock
    private LoanApplicationPersistencePort persistencePort;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private SubmitLoanApplicationService submitLoanApplicationService;

    private static final UUID CUSTOMER_ID = UUID.randomUUID();
    private static final UUID PRODUCT_ID = UUID.randomUUID();
    private static final UUID APP_ID = UUID.randomUUID();
    private Customer customer;
    private LoanProduct product;

    @BeforeEach
    void setUp() {
        customer = Customer.builder().id(CUSTOMER_ID).civilId("c1").fullName("Nguyen A").build();
        product = LoanProduct.builder().id(PRODUCT_ID).code("P1").name("Vay tieu dung")
                .minAmount(BigDecimal.ONE).maxAmount(BigDecimal.TEN).minTermMonths(6).maxTermMonths(36)
                .interestRateAnnual(BigDecimal.ZERO).active(true).build();
        when(loadCustomerPort.findById(CUSTOMER_ID)).thenReturn(Optional.of(customer));
        when(loadLoanProductPort.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
        LocalDateTime now = LocalDateTime.now();
        when(persistencePort.save(any(LoanApplication.class))).thenAnswer(inv -> {
            LoanApplication app = inv.getArgument(0);
            return LoanApplication.builder()
                    .id(APP_ID)
                    .customer(app.getCustomer())
                    .product(app.getProduct())
                    .submittedBy(app.getSubmittedBy())
                    .amount(app.getAmount())
                    .termMonths(app.getTermMonths())
                    .status(app.getStatus())
                    .createdAt(now)
                    .updatedAt(now)
                    .build();
        });
    }

    @Test
    void submit_createsApplication_andPublishesEvent() {
        var command = new me.mb.alps.application.port.in.user.SubmitLoanApplicationUseCase.SubmitLoanCommand(
                CUSTOMER_ID, PRODUCT_ID, new BigDecimal("5000000"), 12, null
        );

        UUID id = submitLoanApplicationService.submit(command);

        assertThat(id).isEqualTo(APP_ID);
        ArgumentCaptor<LoanApplication> captor = ArgumentCaptor.forClass(LoanApplication.class);
        verify(persistencePort).save(captor.capture());
        LoanApplication saved = captor.getValue();
        assertThat(saved.getCustomer()).isEqualTo(customer);
        assertThat(saved.getProduct()).isEqualTo(product);
        assertThat(saved.getAmount()).isEqualByComparingTo("5000000");
        assertThat(saved.getTermMonths()).isEqualTo(12);
        assertThat(saved.getStatus()).isEqualTo(LoanStatus.SUBMITTED);
        assertThat(saved.getSubmittedBy()).isNull();
        verify(eventPublisher).publishEvent(any(me.mb.alps.application.event.LoanApplicationSubmittedEvent.class));
    }

    @Test
    void submit_customerNotFound_throwsNotFoundException() {
        when(loadCustomerPort.findById(CUSTOMER_ID)).thenReturn(Optional.empty());
        var command = new me.mb.alps.application.port.in.user.SubmitLoanApplicationUseCase.SubmitLoanCommand(
                CUSTOMER_ID, PRODUCT_ID, BigDecimal.ONE, 12, null
        );

        assertThatThrownBy(() -> submitLoanApplicationService.submit(command))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Customer");
    }

    @Test
    void submit_productNotFound_throwsNotFoundException() {
        when(loadLoanProductPort.findById(PRODUCT_ID)).thenReturn(Optional.empty());
        var command = new me.mb.alps.application.port.in.user.SubmitLoanApplicationUseCase.SubmitLoanCommand(
                CUSTOMER_ID, PRODUCT_ID, BigDecimal.ONE, 12, null
        );

        assertThatThrownBy(() -> submitLoanApplicationService.submit(command))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Product");
    }
}
