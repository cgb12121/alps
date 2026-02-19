package me.mb.alps.application.service.admin;

import lombok.RequiredArgsConstructor;
import me.mb.alps.application.port.in.admin.CreateCustomerUseCase;
import me.mb.alps.application.port.out.SaveCustomerPort;
import me.mb.alps.domain.entity.Customer;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreateCustomerService implements CreateCustomerUseCase {

    private final SaveCustomerPort saveCustomerPort;

    @Override
    public UUID create(CreateCustomerCommand command) {
        // Khách sau khi khai báo đủ thông tin sẽ có 600 điểm CIC nội bộ
        boolean hasRequiredInfo = command.civilId() != null && command.fullName() != null
                && command.monthlyIncome() != null && command.employmentStatus() != null;
        int initialCreditScore = hasRequiredInfo ? 600 : (command.creditScore() != null ? command.creditScore() : 0);

        Customer customer = Customer.builder()
                .civilId(command.civilId())
                .fullName(command.fullName())
                .email(command.email())
                .phoneNumber(command.phoneNumber())
                .monthlyIncome(command.monthlyIncome())
                .creditScore(initialCreditScore)
                .employmentStatus(command.employmentStatus())
                .age(command.age() != null ? command.age() : 0)
                .build();
        return saveCustomerPort.save(customer).getId();
    }
}
