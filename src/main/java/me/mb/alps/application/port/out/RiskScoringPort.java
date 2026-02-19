package me.mb.alps.application.port.out;

import me.mb.alps.domain.enums.RiskDecision;

/**
 * Outbound port: run risk scoring (Drools). Input is a fact; output is score + decision + reasons.
 */
public interface RiskScoringPort {
    RiskResult score(LoanScoringFact fact);

    record LoanScoringFact(
            double amount,
            int termMonths,
            double monthlyIncome,
            int creditScore,
            int age
    ) {}

    record RiskResult(int riskScore, RiskDecision decision, String ruleReasons, java.math.BigDecimal interestRateAnnual) {}
}
