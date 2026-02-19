package me.mb.alps.domain.enums;

/**
 * Trạng thái thanh toán một kỳ trong lịch trả nợ.
 */
public enum PaymentStatus {
    PENDING,           // Chưa đến hạn
    PAID,              // Đã trả đủ
    PARTIALLY_PAID,    // Trả một phần
    OVERDUE            // Quá hạn chưa trả
}
