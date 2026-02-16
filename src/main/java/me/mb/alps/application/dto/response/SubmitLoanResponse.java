package me.mb.alps.application.dto.response;

import me.mb.alps.domain.enums.LoanStatus;

import java.util.UUID;

/**
 * HTTP response after creating a loan application. Trả về ngay với status SUBMITTED (pending);
 * process chạy async, client có thể poll GET (sau khi có endpoint) để xem APPROVED/REJECTED/REVIEW_REQUIRED.
 */
public record SubmitLoanResponse(UUID id, LoanStatus status) {
    public SubmitLoanResponse(UUID id) {
        this(id, LoanStatus.SUBMITTED);
    }
}
