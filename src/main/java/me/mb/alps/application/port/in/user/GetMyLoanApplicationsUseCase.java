package me.mb.alps.application.port.in.user;

import me.mb.alps.application.dto.response.LoanApplicationSummaryResponse;
import me.mb.alps.domain.enums.UserRole;

import java.util.List;
import java.util.UUID;

/**
 * CUSTOMER xem danh sách loan của mình (theo customerId gắn với user).
 * Thông tin caller được truyền tường minh (userId + role).
 */
public interface GetMyLoanApplicationsUseCase {
    List<LoanApplicationSummaryResponse> listMyLoans(UUID userId, UserRole role);
}
