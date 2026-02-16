package me.mb.alps.application.port.out;

import me.mb.alps.domain.entity.RiskAssessment;

/**
 * Outbound port: persist risk assessment. Implemented by infrastructure.persistence.adapter.
 */
public interface SaveRiskAssessmentPort {
    RiskAssessment save(RiskAssessment assessment);
}
