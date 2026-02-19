package me.mb.alps.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.concurrent.DelegatingSecurityContextExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * @Async chạy trên virtual thread (Java 21+) để không tốn platform thread khi xử lý sau submit.
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "applicationTaskExecutor")
    public Executor applicationTaskExecutor() {
        Executor delegate = Executors.newVirtualThreadPerTaskExecutor();
        return new DelegatingSecurityContextExecutor(delegate);
    }
}
