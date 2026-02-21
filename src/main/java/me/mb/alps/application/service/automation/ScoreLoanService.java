package me.mb.alps.application.service.automation;

import lombok.RequiredArgsConstructor;
import me.mb.alps.application.event.LoanApplicationDecidedEvent;
import me.mb.alps.application.exception.NotFoundException;
import me.mb.alps.application.port.in.automation.ScoreLoanUseCase;
import me.mb.alps.application.port.out.LoanApplicationPersistencePort;
import me.mb.alps.application.port.out.RiskScoringPort;
import me.mb.alps.application.port.out.SaveRiskAssessmentPort;
import me.mb.alps.domain.entity.Customer;
import me.mb.alps.domain.entity.LoanApplication;
import me.mb.alps.domain.entity.RiskAssessment;
import me.mb.alps.domain.enums.LoanStatus;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Use case: load application + customer, run Drools, save RiskAssessment, update application status.
 */
@Service
@RequiredArgsConstructor
public class ScoreLoanService implements ScoreLoanUseCase {

    private final LoanApplicationPersistencePort applicationPort;
    private final RiskScoringPort riskScoringPort;
    private final SaveRiskAssessmentPort saveRiskAssessmentPort;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public LoanStatus score(UUID applicationId) {
        LoanApplication application = applicationPort.findById(applicationId)
                .orElseThrow(() -> new NotFoundException("LoanApplication", applicationId));

        Customer customer = application.getCustomer();
        double income = customer.getMonthlyIncome() != null
                ? customer.getMonthlyIncome().doubleValue()
                : 0;
        RiskScoringPort.LoanScoringFact fact = new RiskScoringPort.LoanScoringFact(
                application.getAmount().doubleValue(),
                application.getTermMonths(),
                income,
                customer.getCreditScore(),
                customer.getAge()
        );

        RiskScoringPort.RiskResult result = riskScoringPort.score(fact);

        RiskAssessment assessment = RiskAssessment.builder()
                .application(application)
                .riskScore(result.riskScore())
                .decision(result.decision())
                .ruleReasons(result.ruleReasons())
                .assessedAt(LocalDateTime.now())
                .build();
        saveRiskAssessmentPort.save(assessment);

        LoanStatus newStatus = switch (result.decision()) {
            case AUTO_APPROVE -> LoanStatus.APPROVED;
            case AUTO_REJECT -> LoanStatus.REJECTED;
            case MANUAL_CHECK -> LoanStatus.REVIEW_REQUIRED;
        };
        application.applyScoringResult(newStatus, result.interestRateAnnual());
        LoanApplication saved = applicationPort.save(application);
        if (newStatus == LoanStatus.APPROVED || newStatus == LoanStatus.REJECTED) {
            eventPublisher.publishEvent(new LoanApplicationDecidedEvent(applicationId, newStatus));
        }
        return saved.getStatus();
    }
}
