package me.mb.alps.application.service.admin;

import me.mb.alps.application.port.out.SaveLoanProductPort;
import me.mb.alps.domain.entity.LoanProduct;
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
class CreateLoanProductServiceTest {

    @Mock
    private SaveLoanProductPort saveLoanProductPort;

    @InjectMocks
    private CreateLoanProductService createLoanProductService;

    private static final UUID SAVED_ID = UUID.randomUUID();

    @Test
    void create_savesProductAndReturnsId() {
        when(saveLoanProductPort.save(any(LoanProduct.class))).thenAnswer(inv -> {
            LoanProduct p = inv.getArgument(0);
            return LoanProduct.builder().id(SAVED_ID).code(p.getCode()).name(p.getName())
                    .minAmount(p.getMinAmount()).maxAmount(p.getMaxAmount())
                    .minTermMonths(p.getMinTermMonths()).maxTermMonths(p.getMaxTermMonths())
                    .interestRateAnnual(p.getInterestRateAnnual()).active(p.isActive()).build();
        });

        var command = new me.mb.alps.application.port.in.admin.CreateLoanProductUseCase.CreateLoanProductCommand(
                "VD", "Vay tieu dung", new BigDecimal("1000000"), new BigDecimal("100000000"),
                6, 36, new BigDecimal("12.5"), true
        );
        UUID id = createLoanProductService.create(command);

        assertThat(id).isEqualTo(SAVED_ID);
        ArgumentCaptor<LoanProduct> captor = ArgumentCaptor.forClass(LoanProduct.class);
        verify(saveLoanProductPort).save(captor.capture());
        LoanProduct saved = captor.getValue();
        assertThat(saved.getCode()).isEqualTo("VD");
        assertThat(saved.getName()).isEqualTo("Vay tieu dung");
        assertThat(saved.getMinAmount()).isEqualByComparingTo("1000000");
        assertThat(saved.getMaxAmount()).isEqualByComparingTo("100000000");
        assertThat(saved.getInterestRateAnnual()).isEqualByComparingTo("12.5");
    }
}
