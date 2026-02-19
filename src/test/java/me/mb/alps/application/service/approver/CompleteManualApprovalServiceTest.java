package me.mb.alps.application.service.approver;

import me.mb.alps.application.exception.NotFoundException;
import me.mb.alps.application.port.out.LoanApplicationPersistencePort;
import me.mb.alps.application.port.out.LoadUserPort;
import me.mb.alps.application.port.out.PublishMessagePort;
import me.mb.alps.domain.entity.Customer;
import me.mb.alps.domain.entity.LoanApplication;
import me.mb.alps.domain.entity.LoanProduct;
import me.mb.alps.domain.enums.LoanStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CompleteManualApprovalServiceTest {

    @Mock
    private LoanApplicationPersistencePort persistencePort;
    @Mock
    private LoadUserPort loadUserPort;
    @Mock
    private PublishMessagePort publishMessagePort;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private CompleteManualApprovalService completeManualApprovalService;

    private static final UUID APP_ID = UUID.randomUUID();
    private LoanApplication application;

    @BeforeEach
    void setUp() {
        var customer = Customer.builder().id(UUID.randomUUID()).civilId("c1").fullName("A").build();
        var product = LoanProduct.builder().id(UUID.randomUUID()).code("P1").name("Vay")
                .minAmount(BigDecimal.ONE).maxAmount(BigDecimal.TEN).minTermMonths(6).maxTermMonths(36)
                .interestRateAnnual(BigDecimal.ZERO).active(true).build();
        application = LoanApplication.builder()
                .id(APP_ID)
                .customer(customer)
                .product(product)
                .amount(BigDecimal.TEN)
                .termMonths(12)
                .status(LoanStatus.REVIEW_REQUIRED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        when(persistencePort.findById(APP_ID)).thenReturn(Optional.of(application));
        when(persistencePort.save(any(LoanApplication.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void complete_approve_updatesStatusAndPublishesMessageAndEvent() {
        var command = new me.mb.alps.application.port.in.approver.CompleteManualApprovalUseCase.CompleteManualApprovalCommand(
                APP_ID, true, null, null
        );

        completeManualApprovalService.complete(command);

        assertThat(application.getStatus()).isEqualTo(LoanStatus.APPROVED);
        verify(publishMessagePort).publish(eq("approvalDecision"), eq(APP_ID.toString()), any(Map.class));
        verify(persistencePort).save(application);
        verify(eventPublisher).publishEvent(any(me.mb.alps.application.event.LoanApplicationDecidedEvent.class));
    }

    @Test
    void complete_reject_setsRejected() {
        var command = new me.mb.alps.application.port.in.approver.CompleteManualApprovalUseCase.CompleteManualApprovalCommand(
                APP_ID, false, null, null
        );

        completeManualApprovalService.complete(command);

        assertThat(application.getStatus()).isEqualTo(LoanStatus.REJECTED);
    }

    @Test
    void complete_applicationNotFound_throwsNotFoundException() {
        when(persistencePort.findById(APP_ID)).thenReturn(Optional.empty());
        var command = new me.mb.alps.application.port.in.approver.CompleteManualApprovalUseCase.CompleteManualApprovalCommand(
                APP_ID, true, null, null
        );

        assertThatThrownBy(() -> completeManualApprovalService.complete(command))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("LoanApplication");
    }

    @Test
    void complete_statusNotReviewRequired_throwsIllegalStateException() {
        application.setStatus(LoanStatus.APPROVED);
        var command = new me.mb.alps.application.port.in.approver.CompleteManualApprovalUseCase.CompleteManualApprovalCommand(
                APP_ID, true, null, null
        );

        assertThatThrownBy(() -> completeManualApprovalService.complete(command))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not pending manual approval");
    }
}
