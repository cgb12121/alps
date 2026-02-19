package me.mb.alps.application.port.in.user;

import me.mb.alps.application.dto.response.RepaymentScheduleItemResponse;
import me.mb.alps.domain.enums.UserRole;

import java.util.List;
import java.util.UUID;

/**
 * CUSTOMER xem lịch trả nợ đã được generate (pre-computed) từ DB.
 * Thông tin caller (userId + role) được truyền từ controller.
 */
public interface GetRepaymentScheduleUseCase {
    List<RepaymentScheduleItemResponse> getSchedule(UUID applicationId, UUID userId, UserRole role);
}
