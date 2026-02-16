package me.mb.alps.application.port.in.admin;

import java.util.UUID;

public interface CreateCustomerUseCase {

    UUID create(CreateCustomerCommand command);

    record CreateCustomerCommand(
            String civilId,
            String fullName,
            String email,
            String phoneNumber,
            java.math.BigDecimal monthlyIncome,
            Integer creditScore,
            me.mb.alps.domain.enums.EmploymentStatus employmentStatus,
            Integer age
    ) {}
}
