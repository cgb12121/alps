package me.mb.alps.application.service.automation;

import me.mb.alps.application.exception.NotFoundException;
import me.mb.alps.application.port.out.LoanApplicationPersistencePort;
import me.mb.alps.application.port.out.RiskScoringPort;
import me.mb.alps.application.port.out.SaveRiskAssessmentPort;
import me.mb.alps.domain.entity.Customer;
import me.mb.alps.domain.entity.LoanApplication;
import me.mb.alps.domain.entity.LoanProduct;
import me.mb.alps.domain.enums.LoanStatus;
import me.mb.alps.domain.enums.RiskDecision;
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
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ScoreLoanServiceTest {

    @Mock
    private LoanApplicationPersistencePort applicationPort;
    @Mock
    private RiskScoringPort riskScoringPort;
    @Mock
    private SaveRiskAssessmentPort saveRiskAssessmentPort;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ScoreLoanService scoreLoanService;

    private static final UUID APP_ID = UUID.randomUUID();
    private LoanApplication application;

    @BeforeEach
    void setUp() {
        var customer = Customer.builder().id(UUID.randomUUID()).civilId("c1").fullName("A")
                .monthlyIncome(new BigDecimal("20000000")).creditScore(70).age(30).build();
        var product = LoanProduct.builder().id(UUID.randomUUID()).code("P1").name("Vay")
                .minAmount(BigDecimal.ONE).maxAmount(BigDecimal.TEN).minTermMonths(6).maxTermMonths(36)
                .interestRateAnnual(BigDecimal.ZERO).active(true).build();
        application = LoanApplication.builder()
                .id(APP_ID)
                .customer(customer)
                .product(product)
                .amount(new BigDecimal("10000000"))
                .termMonths(12)
                .status(LoanStatus.SUBMITTED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        when(applicationPort.findById(APP_ID)).thenReturn(Optional.of(application));
        when(applicationPort.save(any(LoanApplication.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void score_autoApprove_returnsApprovedAndPublishesEvent() {
        when(riskScoringPort.score(any())).thenReturn(new RiskScoringPort.RiskResult(
                85, RiskDecision.AUTO_APPROVE, "Good", java.math.BigDecimal.valueOf(8.0)));

        LoanStatus result = scoreLoanService.score(APP_ID);

        assertThat(result).isEqualTo(LoanStatus.APPROVED);
        assertThat(application.getStatus()).isEqualTo(LoanStatus.APPROVED);
        verify(saveRiskAssessmentPort).save(any());
        verify(eventPublisher).publishEvent(any(me.mb.alps.application.event.LoanApplicationDecidedEvent.class));
    }

    @Test
    void score_autoReject_returnsRejected() {
        when(riskScoringPort.score(any())).thenReturn(new RiskScoringPort.RiskResult(
                30, RiskDecision.AUTO_REJECT, "Low", java.math.BigDecimal.valueOf(25.0)));

        LoanStatus result = scoreLoanService.score(APP_ID);

        assertThat(result).isEqualTo(LoanStatus.REJECTED);
        assertThat(application.getStatus()).isEqualTo(LoanStatus.REJECTED);
    }

    @Test
    void score_manualCheck_returnsReviewRequiredAndNoEvent() {
        when(riskScoringPort.score(any())).thenReturn(new RiskScoringPort.RiskResult(
                50, RiskDecision.MANUAL_CHECK, "Review", java.math.BigDecimal.valueOf(15.0)));

        LoanStatus result = scoreLoanService.score(APP_ID);

        assertThat(result).isEqualTo(LoanStatus.REVIEW_REQUIRED);
        assertThat(application.getStatus()).isEqualTo(LoanStatus.REVIEW_REQUIRED);
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void score_applicationNotFound_throwsNotFoundException() {
        when(applicationPort.findById(APP_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> scoreLoanService.score(APP_ID))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("LoanApplication");
    }
}
