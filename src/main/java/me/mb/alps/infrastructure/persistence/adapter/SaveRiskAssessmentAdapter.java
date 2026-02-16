package me.mb.alps.infrastructure.persistence.adapter;

import lombok.RequiredArgsConstructor;
import me.mb.alps.application.port.out.SaveRiskAssessmentPort;
import me.mb.alps.domain.entity.RiskAssessment;
import me.mb.alps.infrastructure.persistence.jpa.RiskAssessmentJpaRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SaveRiskAssessmentAdapter implements SaveRiskAssessmentPort {

    private final RiskAssessmentJpaRepository jpaRepository;

    @Override
    public RiskAssessment save(RiskAssessment assessment) {
        return jpaRepository.save(assessment);
    }
}
