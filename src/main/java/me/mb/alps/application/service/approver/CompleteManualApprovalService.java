package me.mb.alps.application.service.approver;

import lombok.RequiredArgsConstructor;
import me.mb.alps.application.event.LoanApplicationDecidedEvent;
import me.mb.alps.application.exception.NotFoundException;
import me.mb.alps.application.port.in.approver.CompleteManualApprovalUseCase;
import me.mb.alps.application.port.out.ApprovalHistoryPersistencePort;
import me.mb.alps.application.port.out.LoanApplicationPersistencePort;
import me.mb.alps.application.port.out.LoadUserPort;
import me.mb.alps.application.port.out.PublishMessagePort;
import me.mb.alps.domain.entity.ApprovalHistory;
import me.mb.alps.domain.entity.LoanApplication;
import me.mb.alps.domain.entity.User;
import me.mb.alps.domain.enums.LoanStatus;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CompleteManualApprovalService implements CompleteManualApprovalUseCase {

    private static final String MESSAGE_APPROVAL_DECISION = "approvalDecision";

    private final LoanApplicationPersistencePort persistencePort;
    private final LoadUserPort loadUserPort;
    private final PublishMessagePort publishMessagePort;
    private final ApplicationEventPublisher eventPublisher;
    private final ApprovalHistoryPersistencePort approvalHistoryPort;

    @Override
    @Transactional
    public void complete(CompleteManualApprovalCommand command) {
        LoanApplication application = persistencePort.findById(command.applicationId())
                .orElseThrow(() -> new NotFoundException("LoanApplication", command.applicationId()));
        User reviewer = command.reviewedByUserId() != null
                ? loadUserPort.findById(command.reviewedByUserId()).orElse(null)
                : null;

        publishMessagePort.publish(
                MESSAGE_APPROVAL_DECISION,
                application.getId().toString(),
                Map.of(
                        "approved", command.approved(),
                        "reviewedByUserId", command.reviewedByUserId() != null ? command.reviewedByUserId().toString() : ""
                )
        );

        LoanStatus oldStatus = application.getStatus();
        application.completeManualApproval(command.approved(), reviewer, LocalDateTime.now());
        LoanStatus newStatus = application.getStatus();
        persistencePort.save(application);

        // Lưu lịch sử duyệt
        if (reviewer != null) {
            ApprovalHistory history = ApprovalHistory.builder()
                    .loanApplication(application)
                    .approvedBy(reviewer)
                    .oldStatus(oldStatus)
                    .newStatus(newStatus)
                    .comment(command.comment())
                    .build();
            approvalHistoryPort.save(history);
        }

        eventPublisher.publishEvent(new LoanApplicationDecidedEvent(application.getId(), newStatus));
    }
}
