package me.mb.alps.infrastructure.rules;

import me.mb.alps.application.port.out.RiskScoringPort;
import me.mb.alps.domain.enums.RiskDecision;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for Drools scoring: no Camunda, no Spring. Builds KieContainer from the classpath and runs rules.
 */
class DroolsRiskScoringAdapterTest {

    private RiskScoringPort riskScoringPort;

    @BeforeEach
    void setUp() {
        KieContainer kieContainer = KieServices.Factory.get().getKieClasspathContainer();
        riskScoringPort = new DroolsRiskScoringAdapter(kieContainer);
    }

    @Test
    void autoApprove_whenGoodProfile() {
        var fact = new RiskScoringPort.LoanScoringFact(
                10_000_000, 12, 20_000_000, 75, 30
        );
        RiskScoringPort.RiskResult result = riskScoringPort.score(fact);
        assertThat(result.decision()).isEqualTo(RiskDecision.AUTO_APPROVE);
        assertThat(result.riskScore()).isEqualTo(85);
        assertThat(result.ruleReasons()).contains("Good credit");
        assertThat(result.interestRateAnnual()).isNotNull();
    }

    @Test
    void autoReject_whenLowCreditScore() {
        var fact = new RiskScoringPort.LoanScoringFact(
                5_000_000, 12, 15_000_000, 40, 28
        );
        RiskScoringPort.RiskResult result = riskScoringPort.score(fact);
        assertThat(result.decision()).isEqualTo(RiskDecision.AUTO_REJECT);
        assertThat(result.ruleReasons()).contains("Credit score too low");
    }

    @Test
    void autoReject_whenInsufficientIncome() {
        var fact = new RiskScoringPort.LoanScoringFact(
                100_000_000, 12, 5_000_000, 70, 35
        );
        RiskScoringPort.RiskResult result = riskScoringPort.score(fact);
        assertThat(result.decision()).isEqualTo(RiskDecision.AUTO_REJECT);
        assertThat(result.ruleReasons()).contains("Debt-to-income");
    }

    @Test
    void manualCheck_whenNoRuleFires() {
        var fact = new RiskScoringPort.LoanScoringFact(
                10_000_000, 12, 10_000_000, 55, 25
        );
        RiskScoringPort.RiskResult result = riskScoringPort.score(fact);
        assertThat(result.decision()).isEqualTo(RiskDecision.MANUAL_CHECK);
    }
}
