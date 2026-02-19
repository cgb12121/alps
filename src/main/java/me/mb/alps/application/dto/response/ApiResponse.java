package me.mb.alps.application.dto.response;

import java.time.OffsetDateTime;
import java.time.ZoneId;

/**
 * Generic normalized API response wrapper.
 *
 * @param <T> type of data payload
 */
public record ApiResponse<T>(
        boolean success,
        T data,
        String message,
        OffsetDateTime timestamp
) {

    private static final ZoneId ZONE_ID = ZoneId.systemDefault();

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null, OffsetDateTime.now(ZONE_ID));
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, data, message, OffsetDateTime.now(ZONE_ID));
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, null, message, OffsetDateTime.now(ZONE_ID));
    }
}


