package me.mb.alps.application.port.out;

import java.util.Map;

/**
 * Outbound port: publish a message to Zeebe (correlate to a process instance waiting at a message catch event).
 */
public interface PublishMessagePort {
    void publish(String messageName, String correlationKey, Map<String, Object> variables);
}
