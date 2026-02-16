package me.mb.alps.infrastructure.workflow;

import me.mb.alps.application.port.out.StartProcessPort;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * No-op StartProcessPort khi Camunda client tắt. Gọi startProcess trả về 0, không start process thật.
 */
@Component
@ConditionalOnProperty(name = "camunda.client.enabled", havingValue = "false", matchIfMissing = true)
public class StartProcessPortStub implements StartProcessPort {

    @Override
    public long startProcess(String processId, Map<String, Object> variables) {
        return 0L;
    }
}
