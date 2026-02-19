package me.mb.alps.infrastructure.web.admin;

import lombok.RequiredArgsConstructor;
import me.mb.alps.application.dto.response.ApiResponse;
import me.mb.alps.application.dto.response.LoanApplicationSummaryResponse;
import me.mb.alps.application.port.in.admin.ListLoansUseCase;
import me.mb.alps.domain.enums.LoanStatus;
import org.jspecify.annotations.NonNull;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * ADMIN: List và quản lý loans với filters.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/loans")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Loans", description = "Admin APIs to search and inspect loans")
public class AdminLoanController {

    private final ListLoansUseCase listLoansUseCase;

    @GetMapping
    @Operation(
            summary = "Search loans",
            description = "Admin search loans with optional filters: status, reviewer, and createdAt date range."
    )
    public ResponseEntity<@NonNull ApiResponse<List<LoanApplicationSummaryResponse>>> list(
            @RequestParam(required = false) LoanStatus status,
            @RequestParam(required = false) UUID reviewedById,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate
    ) {
        var command = new ListLoansUseCase.ListLoansCommand(status, reviewedById, fromDate, toDate);
        List<LoanApplicationSummaryResponse> loans = listLoansUseCase.list(command);
        return ResponseEntity.ok(ApiResponse.success(loans));
    }
}
