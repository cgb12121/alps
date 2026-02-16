package me.mb.alps.application.port.out;

import java.util.Map;

/**
 * Outbound port: start a Camunda/Zeebe process instance. Returns process instance key.
 */
public interface StartProcessPort {
    long startProcess(String processId, Map<String, Object> variables);
}
