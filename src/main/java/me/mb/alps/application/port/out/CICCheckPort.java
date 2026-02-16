package me.mb.alps.application.port.out;

import java.util.UUID;

/**
 * Outbound port: giả lập tra cứu CIC (Trung tâm tín dụng). Có độ trễ mạng và random nợ xấu.
 */
public interface CICCheckPort {
    CICResult check(UUID applicationId);

    record CICResult(boolean clean, String reportId) {}
}
