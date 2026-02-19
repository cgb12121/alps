package me.mb.alps.application.service.admin;

import me.mb.alps.application.port.out.SaveCustomerPort;
import me.mb.alps.domain.entity.Customer;
import me.mb.alps.domain.enums.EmploymentStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateCustomerServiceTest {

    @Mock
    private SaveCustomerPort saveCustomerPort;

    @InjectMocks
    private CreateCustomerService createCustomerService;

    private static final UUID SAVED_ID = UUID.randomUUID();

    @Test
    void create_savesCustomerAndReturnsId() {
        when(saveCustomerPort.save(any(Customer.class))).thenAnswer(inv -> {
            Customer c = inv.getArgument(0);
            return Customer.builder().id(SAVED_ID).civilId(c.getCivilId()).fullName(c.getFullName())
                    .email(c.getEmail()).phoneNumber(c.getPhoneNumber()).monthlyIncome(c.getMonthlyIncome())
                    .creditScore(c.getCreditScore()).employmentStatus(c.getEmploymentStatus()).age(c.getAge()).build();
        });

        var command = new me.mb.alps.application.port.in.admin.CreateCustomerUseCase.CreateCustomerCommand(
                "123", "Nguyen Van A", "a@b.com", "0901234567",
                new BigDecimal("15000000"), 75, EmploymentStatus.SALARIED, 30
        );
        UUID id = createCustomerService.create(command);

        assertThat(id).isEqualTo(SAVED_ID);
        ArgumentCaptor<Customer> captor = ArgumentCaptor.forClass(Customer.class);
        verify(saveCustomerPort).save(captor.capture());
        Customer saved = captor.getValue();
        assertThat(saved.getCivilId()).isEqualTo("123");
        assertThat(saved.getFullName()).isEqualTo("Nguyen Van A");
        assertThat(saved.getCreditScore()).isEqualTo(75);
        assertThat(saved.getAge()).isEqualTo(30);
    }
}
