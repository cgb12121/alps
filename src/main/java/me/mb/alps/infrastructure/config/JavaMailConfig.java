package me.mb.alps.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Configuration
@ConditionalOnBooleanProperty(name = "alps.notification.mail.enabled")
public class JavaMailConfig {

    @Bean
    public JavaMailSender mailSender(
            @Value("${spring.mail.javamail.username:}") String username,
            @Value("${spring.mail.javamail.password:}") String password) {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("smtp.gmail.com");
        mailSender.setPort(587);
        mailSender.setProtocol(JavaMailSenderImpl.DEFAULT_PROTOCOL);
        mailSender.setDefaultEncoding("UTF-8");
        mailSender.setUsername(username);
        mailSender.setPassword(password);
        return mailSender;
    }

}
