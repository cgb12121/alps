package me.mb.alps.domain.enums;

/**
 * Mỗi account chỉ có 1 role. ADMIN tạo IT/APPROVER; IT tạo APPROVER; CUSTOMER tự đăng ký.
 */
public enum UserRole {
    CUSTOMER,
    APPROVER,
    IT,
    ADMIN
}
