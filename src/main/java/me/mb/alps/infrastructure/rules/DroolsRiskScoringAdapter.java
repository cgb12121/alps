package me.mb.alps.infrastructure.rules;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.mb.alps.application.port.out.RiskScoringPort;
import me.mb.alps.domain.enums.RiskDecision;
import me.mb.alps.infrastructure.rules.dto.LoanScoringFactBean;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.springframework.stereotype.Component;

/**
 * Implements RiskScoringPort using Drools (DRL from classpath). Uses kmodule.xml + loan-scoring.drl.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DroolsRiskScoringAdapter implements RiskScoringPort {

    private final KieContainer kieContainer;

    @Override
    public RiskResult score(LoanScoringFact fact) {
        log.debug("Drools: scoring fact amount={}, creditScore={}, firing rules", fact.amount(), fact.creditScore());
        KieSession session = kieContainer.newKieSession("loanScoringSession");
        try {
            LoanScoringFactBean bean = LoanScoringFactBean.from(
                    fact.amount(),
                    fact.termMonths(),
                    fact.monthlyIncome(),
                    fact.creditScore(),
                    fact.age()
            );
            session.insert(bean);
            session.fireAllRules();
            RiskDecision decision = bean.getDecision() != null
                    ? bean.getDecision()
                    : RiskDecision.MANUAL_CHECK;
            int score = bean.getRiskScore();
            String reasons = bean.getRuleReasons() != null
                    ? bean.getRuleReasons()
                    : "No rule fired";
            java.math.BigDecimal interestRate = bean.getInterestRateAnnual() != null
                    ? bean.getInterestRateAnnual()
                    : java.math.BigDecimal.valueOf(12.0); // Default 12%/year if not set
            log.info("Drools: result decision={}, score={}, interestRate={}%, reasons={}", decision, score, interestRate, reasons);
            return new RiskResult(score, decision, reasons, interestRate);
        } finally {
            session.dispose();
        }
    }
}
