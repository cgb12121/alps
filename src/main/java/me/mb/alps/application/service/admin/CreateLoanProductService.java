package me.mb.alps.application.service.admin;

import lombok.RequiredArgsConstructor;
import me.mb.alps.application.port.in.admin.CreateLoanProductUseCase;
import me.mb.alps.application.port.out.SaveLoanProductPort;
import me.mb.alps.domain.entity.LoanProduct;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreateLoanProductService implements CreateLoanProductUseCase {

    private final SaveLoanProductPort saveLoanProductPort;

    @Override
    public UUID create(CreateLoanProductCommand command) {
        LoanProduct product = LoanProduct.builder()
                .code(command.code())
                .name(command.name())
                .minAmount(command.minAmount())
                .maxAmount(command.maxAmount())
                .minTermMonths(command.minTermMonths())
                .maxTermMonths(command.maxTermMonths())
                .interestRateAnnual(command.interestRateAnnual())
                .active(command.active())
                .build();
        return saveLoanProductPort.save(product).getId();
    }
}
