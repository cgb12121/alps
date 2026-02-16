# Cấu trúc Application layer – chia theo actor

Khi mở rộng, có thể chia use case theo **ai gọi** (user / nhân viên duyệt vay / system automation).

## 1. Package structure đề xuất

Giữ nguyên `port.out`, `dto`, `event`, `exception` dùng chung; chỉ chia **port.in** và **service** theo actor:

```
application/
├── config/
├── dto/
│   ├── request/
│   └── response/
├── event/
├── exception/
├── port/
│   ├── in/
│   │   ├── user/              ← Khách hàng / người nộp hồ sơ
│   │   │   ├── SubmitLoanApplicationUseCase.java
│   │   │   └── GetMyLoanApplicationsUseCase.java   (sau này)
│   │   ├── approver/          ← Nhân viên duyệt vay
│   │   │   ├── ListPendingApprovalsUseCase.java
│   │   │   └── CompleteManualApprovalUseCase.java
│   │   ├── automation/        ← System / job worker gọi
│   │   │   └── ScoreLoanUseCase.java
│   │   └── admin/             ← Admin / setup (optional)
│   │       ├── CreateUserUseCase.java
│   │       ├── CreateCustomerUseCase.java
│   │       └── CreateLoanProductUseCase.java
│   └── out/                   ← Giữ chung, không chia theo actor
├── service/
│   ├── user/
│   │   └── SubmitLoanApplicationService.java
│   ├── approver/
│   │   ├── ListPendingApprovalsService.java
│   │   └── CompleteManualApprovalService.java
│   ├── automation/
│   │   └── ScoreLoanService.java
│   └── admin/
│       ├── CreateUserService.java
│       └── ...
```

## 2. Ánh xạ actor → use case

| Actor | Mô tả | Use case hiện có | Gợi ý thêm |
|-------|--------|-------------------|-------------|
| **user** | Khách hàng nộp hồ sơ, xem trạng thái | `SubmitLoanApplicationUseCase` | GetMyLoanApplications, GetLoanApplicationDetail |
| **approver** | Nhân viên duyệt vay | `ListPendingApprovalsUseCase`, `CompleteManualApprovalUseCase` | (đủ cho luồng duyệt tay) |
| **automation** | Zeebe job worker / batch | `ScoreLoanUseCase` | (CIC chỉ dùng port, không cần use case riêng) |
| **admin** | Setup danh mục, user | `CreateUserUseCase`, `CreateCustomerUseCase`, `CreateLoanProductUseCase` | — |

## 3. Infrastructure tương ứng (đã áp dụng)

- **User** → `infrastructure.web.user.UserLoanApplicationController` — `/api/user/loan-applications` (POST submit).
- **Approver** → `infrastructure.web.approver.ApprovalController` — `/api/approver/loan-applications/pending-approval`, `/{id}/approve`, `/{id}/reject`.
- **Automation** → `ScoreLoanJobWorker`, `CheckCicJobWorker` (worker gọi use case hoặc port).
- **Admin** → `infrastructure.web.admin.AdminUserController`, `AdminCustomerController`, `AdminLoanProductController` — `/api/admin/users`, `/api/admin/customers`, `/api/admin/loan-products`.

Controller chia theo role để dễ gắn auth: JWT + (sau) @PreAuthorize("hasRole('APPROVER')") v.v.

## 4. Config bean (UseCaseConfig)

Có thể giữ một `UseCaseConfig` hoặc tách theo actor, ví dụ:

- `UseCaseConfig` → tất cả bean use case, hoặc
- `UserUseCaseConfig`, `ApproverUseCaseConfig`, `AutomationUseCaseConfig` (mỗi class `@Bean` cho đúng package in).

## 5. Security (JWT)

- **Package**: `infrastructure.security` — `JwtProperties`, `JwtTokenProvider` (Nimbus HS256), `JwtAuthenticationFilter`, `SecurityConfig`, `AlpsUserDetailsService`, `AlpsUserDetails`.
- **Login**: POST `/api/auth/login` với `{ "username", "password" }` → trả về `{ "token", "username", "userId" }`. Header `Authorization: Bearer <token>` cho mọi request `/api/**`.
- **Cấu hình**: `alps.jwt.secret` (min 32 ký tự), `alps.jwt.validity-ms`, `alps.security.dev-default-password` (khi chưa lưu password trong DB).
- Sau này thêm role vào User và @PreAuthorize("hasRole('APPROVER')") trên từng controller.

## 6. Refactor đã làm

- Đã tách `port.in` và `service` theo user / approver / automation / admin; controller tách tương ứng; security JWT (Nimbus) đã cấu hình.
