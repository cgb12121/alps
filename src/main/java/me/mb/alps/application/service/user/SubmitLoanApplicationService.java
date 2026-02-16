package me.mb.alps.application.service.user;

import lombok.RequiredArgsConstructor;
import me.mb.alps.application.exception.NotFoundException;
import me.mb.alps.application.port.in.user.SubmitLoanApplicationUseCase;
import me.mb.alps.application.port.out.LoadCustomerPort;
import me.mb.alps.application.port.out.LoadLoanProductPort;
import me.mb.alps.application.port.out.LoadUserPort;
import me.mb.alps.application.event.LoanApplicationSubmittedEvent;
import me.mb.alps.application.port.out.LoanApplicationPersistencePort;
import me.mb.alps.domain.entity.Customer;
import me.mb.alps.domain.entity.LoanApplication;
import me.mb.alps.domain.entity.LoanProduct;
import me.mb.alps.domain.entity.User;
import me.mb.alps.domain.enums.LoanStatus;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Use case implementation: submit a loan application. Depends only on port.out (no JPA, no HTTP).
 */
@Service
@RequiredArgsConstructor
public class SubmitLoanApplicationService implements SubmitLoanApplicationUseCase {

    private final LoadCustomerPort loadCustomerPort;
    private final LoadLoanProductPort loadLoanProductPort;
    private final LoadUserPort loadUserPort;
    private final LoanApplicationPersistencePort persistencePort;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public UUID submit(SubmitLoanCommand command) {
        Customer customer = loadCustomerPort.findById(command.customerId())
                .orElseThrow(() -> new NotFoundException("Customer", command.customerId()));
        LoanProduct product = loadLoanProductPort.findById(command.productId())
                .orElseThrow(() -> new NotFoundException("Product", command.productId()));
        User submittedBy = command.submittedByUserId() != null
                ? loadUserPort.findById(command.submittedByUserId()).orElse(null)
                : null;

        LoanApplication application = LoanApplication.builder()
                .customer(customer)
                .product(product)
                .submittedBy(submittedBy)
                .amount(command.amount())
                .termMonths(command.termMonths())
                .status(LoanStatus.SUBMITTED)
                .build();

        LoanApplication saved = persistencePort.save(application);
        eventPublisher.publishEvent(new LoanApplicationSubmittedEvent(saved.getId()));
        return saved.getId();
    }
}
