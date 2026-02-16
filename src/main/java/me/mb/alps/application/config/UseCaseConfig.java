package me.mb.alps.application.config;

import me.mb.alps.application.port.in.admin.CreateCustomerUseCase;
import me.mb.alps.application.port.in.admin.CreateLoanProductUseCase;
import me.mb.alps.application.port.in.admin.CreateUserUseCase;
import me.mb.alps.application.port.in.approver.CompleteManualApprovalUseCase;
import me.mb.alps.application.port.in.approver.ListPendingApprovalsUseCase;
import me.mb.alps.application.port.in.automation.ScoreLoanUseCase;
import me.mb.alps.application.port.in.user.SubmitLoanApplicationUseCase;
import me.mb.alps.application.service.admin.CreateCustomerService;
import me.mb.alps.application.service.admin.CreateLoanProductService;
import me.mb.alps.application.service.admin.CreateUserService;
import me.mb.alps.application.service.approver.CompleteManualApprovalService;
import me.mb.alps.application.service.approver.ListPendingApprovalsService;
import me.mb.alps.application.service.automation.ScoreLoanService;
import me.mb.alps.application.service.user.SubmitLoanApplicationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Exposes use case interfaces as beans. Implementations are in service.* subpackages (user, approver, automation, admin).
 */
@Configuration
public class UseCaseConfig {

    @Bean
    public SubmitLoanApplicationUseCase submitLoanApplicationUseCase(SubmitLoanApplicationService impl) {
        return impl;
    }

    @Bean
    public ListPendingApprovalsUseCase listPendingApprovalsUseCase(ListPendingApprovalsService impl) {
        return impl;
    }

    @Bean
    public CompleteManualApprovalUseCase completeManualApprovalUseCase(CompleteManualApprovalService impl) {
        return impl;
    }

    @Bean
    public ScoreLoanUseCase scoreLoanUseCase(ScoreLoanService impl) {
        return impl;
    }

    @Bean
    public CreateUserUseCase createUserUseCase(CreateUserService impl) {
        return impl;
    }

    @Bean
    public CreateCustomerUseCase createCustomerUseCase(CreateCustomerService impl) {
        return impl;
    }

    @Bean
    public CreateLoanProductUseCase createLoanProductUseCase(CreateLoanProductService impl) {
        return impl;
    }
}
