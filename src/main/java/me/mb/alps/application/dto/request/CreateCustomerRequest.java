package me.mb.alps.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import me.mb.alps.domain.enums.EmploymentStatus;

import java.math.BigDecimal;

/**
 * Request body for creating a customer. ID is generated (UUIDv7).
 */
public record CreateCustomerRequest(
        @NotBlank(message = "civilId is required") String civilId,
        @NotBlank(message = "fullName is required") String fullName,
        String email,
        String phoneNumber,
        BigDecimal monthlyIncome,
        Integer creditScore,
        EmploymentStatus employmentStatus,
        Integer age
) {}
