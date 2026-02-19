package me.mb.alps;

import me.mb.alps.application.port.out.StartProcessPort;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * When Camunda is disabled (test profile), provide a no-op StartProcessPort so SubmitLoanApplicationService can be created.
 */
@TestConfiguration
public class TestConfig {

    @Bean
    @Primary
    public StartProcessPort startProcessPort() {
        return (processId, variables) -> -1L;
    }
}
