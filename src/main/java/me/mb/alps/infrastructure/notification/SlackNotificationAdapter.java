package me.mb.alps.infrastructure.notification;

import me.mb.alps.application.port.out.NotificationPort;
import me.mb.alps.domain.enums.LoanStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.UUID;

/**
 * Gửi thông báo qua Slack Incoming Webhook. Cấu hình alps.notification.slack.webhook-url.
 */
@Component
@ConditionalOnProperty(name = "alps.notification.slack.webhook-url")
public class SlackNotificationAdapter implements NotificationPort {

    private final RestClient restClient;
    private final String webhookUrl;

    public SlackNotificationAdapter(
            RestClient.Builder restClientBuilder,
            @Value("${alps.notification.slack.webhook-url:}") String webhookUrl) {
        this.restClient = restClientBuilder.build();
        this.webhookUrl = webhookUrl;
    }

    @Override
    public void notifyDecision(UUID applicationId, LoanStatus decision, String customerEmail, String customerName) {
        if (webhookUrl == null || webhookUrl.isBlank()) {
            return;
        }
        String text = decision == LoanStatus.APPROVED
                ? "✅ Hồ sơ vay *" + applicationId + "* đã được *chấp thuận*."
                : "❌ Hồ sơ vay *" + applicationId + "* đã bị *từ chối*.";
        restClient.post()
                .uri(webhookUrl)
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .body(Map.of("text", text))
                .retrieve()
                .toBodilessEntity();
    }
}
