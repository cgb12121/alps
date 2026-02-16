package me.mb.alps.infrastructure.cic;

import lombok.extern.slf4j.Slf4j;
import me.mb.alps.application.port.out.CICCheckPort;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Giả lập tra cứu CIC: Thread.sleep(2000) độ trễ mạng, ~20% nợ xấu (random).
 */
@Slf4j
@Component
public class CICCheckAdapter implements CICCheckPort {

    private static final long SIMULATED_DELAY_MS = 2000;
    private static final double BAD_DEBT_PROBABILITY = 0.2;

    @Override
    public CICResult check(UUID applicationId) {
        log.info("CIC check started for application {}", applicationId);
        try {
            Thread.sleep(SIMULATED_DELAY_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("CIC check interrupted", e);
        }
        boolean clean = ThreadLocalRandom.current().nextDouble() >= BAD_DEBT_PROBABILITY;
        String reportId = "CIC-" + applicationId.toString().substring(0, 8) + "-" + System.currentTimeMillis();
        log.info("CIC check done for application {}: clean={}, reportId={}", applicationId, clean, reportId);
        return new CICResult(clean, reportId);
    }
}
