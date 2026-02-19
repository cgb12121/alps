package me.mb.alps.infrastructure.rules.dto;

import lombok.Getter;
import lombok.Setter;
import me.mb.alps.domain.enums.RiskDecision;

/**
 * Mutable fact for Drools rules (DRL cannot modify records). Built from RiskScoringPort.LoanScoringFact.
 */
@Setter
@Getter
public class LoanScoringFactBean {

    private double amount;
    private int termMonths;
    private double monthlyIncome;
    private int creditScore;
    private int age;

    private RiskDecision decision;
    private int riskScore;
    private String ruleReasons;
    /** Lãi suất (%/năm) do Drools risk-based pricing quyết định. */
    private java.math.BigDecimal interestRateAnnual;

    public LoanScoringFactBean() {
    }

    public static LoanScoringFactBean from(double amount, int termMonths, double monthlyIncome, int creditScore, int age) {
        var b = new LoanScoringFactBean();
        b.amount = amount;
        b.termMonths = termMonths;
        b.monthlyIncome = monthlyIncome;
        b.creditScore = creditScore;
        b.age = age;
        return b;
    }

}
