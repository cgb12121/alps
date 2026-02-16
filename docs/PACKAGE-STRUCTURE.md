# Package structure – Clean / Hexagonal

Cấu trúc **domain + application + infrastructure** (không theo kiểu controller/service/repo từng tầng như layered). Dependency hướng vào trong: **infrastructure → application → domain**.

```
me.mb.alps
├── domain                    # Nghiệp vụ thuần (entity, enum, domain service nếu có)
│   ├── entity
│   └── enums
├── application                # Use cases, ports, DTO, exception
│   ├── dto
│   │   ├── request            # DTO nhận từ client (POST body, query)
│   │   └── response           # DTO trả về (JSON response)
│   ├── exception              # Application/Domain exception (NotFoundException, AlpsException)
│   ├── port.in                # Inbound: giao diện “app cung cấp” (use case)
│   ├── port.out               # Outbound: giao diện “app cần” (persistence, workflow, rules)
│   └── service               # Implementation use case (gọi port.out)
└── infrastructure             # Adapters: triển khai port + framework
    ├── persistence
    │   ├── adapter            # Implement port.out (LoanApplicationPersistenceAdapter, Load*Adapter)
    │   └── jpa                # Spring Data JpaRepository (chỉ dùng bởi adapter)
    ├── web                    # REST controller + GlobalExceptionHandler
    ├── workflow
    └── rules
```

## Persistence: adapter vs jpa

Trong **infrastructure.persistence** có hai subpackage:

- **persistence.adapter**: class implement **port.out** (LoanApplicationPersistencePort, LoadCustomerPort, …). Đây là “biên” Hexagonal: application chỉ biết interface, không biết class này.
- **persistence.jpa**: chỉ chứa interface **Spring Data JpaRepository** (LoanApplicationJpaRepository, CustomerJpaRepository, …). Đây là implementation detail: adapter dùng các repo này để thực hiện port, application không thấy và không phụ thuộc JPA.

Lý do tách: (1) rõ ràng vai trò – adapter = boundary, jpa = công cụ; (2) sau này thêm persistence khác (ví dụ MongoDB) có thể thêm persistence.mongo, adapter có thể đổi hoặc có nhiều impl cho cùng một port.

---

## DTO: request + response

- **application.dto.request**: body/param từ HTTP (ví dụ `SubmitLoanRequest`). Dùng `@Valid` ở controller; validation (Jakarta Validation) nằm trên DTO.
- **application.dto.response**: đối tượng trả về API (ví dụ `SubmitLoanResponse`, `LoanApplicationSummaryResponse`). Application layer hoặc adapter map từ domain/entity sang response DTO để không lộ entity ra ngoài.

Controller: nhận **request** DTO → map sang **Command** (input use case) → gọi use case → map kết quả sang **response** DTO.

## Exception

- **application.exception**: exception nghiệp vụ / application (ví dụ `AlpsException`, `NotFoundException`). Use case throw các exception này; **không** throw JPA/Spring exception ra ngoài.
- **infrastructure.web.GlobalExceptionHandler**: map exception → HTTP (404 cho `NotFoundException`, 422 cho `AlpsException`, 400 cho validation). Có thể trả về RFC 7807 ProblemDetail.

---

## Purist vs Pragmatic (domain entity có @Entity hay không)

### Cách đang dùng: **Pragmatic** (domain entity = JPA entity)

- **domain.entity** là class có `@Entity`, map trực tiếp xuống bảng.
- Application và infrastructure đều dùng chung class này. Port.out persistence nhận/trả **domain entity**.
- **Ưu điểm:** Ít code, không cần mapper Domain ↔ JPA. Phù hợp POC / enterprise nhẹ.
- **Nhược điểm:** Domain “dính” JPA (annotation, lazy, v.v.). Đổi ORM hoặc tách domain thuần sau sẽ tốn công hơn.

### Cách Purist (domain POJO + JpaEntity riêng)

- **domain.entity**: POJO thuần (không `@Entity`), không phụ thuộc JPA/Jakarta.
- **infrastructure.persistence**: có thêm class **JpaEntity** (ví dụ `LoanApplicationJpaEntity`) có `@Entity`, map đúng bảng.
- Dùng **MapStruct** (hoặc mapper tay) để đổi: **Domain Entity ↔ JpaEntity** trong adapter. Port.out trả/ nhận **domain entity**; adapter bên trong load/save JpaEntity rồi map sang domain.
- **Ưu điểm:** Domain sạch, không phụ thuộc framework. Dễ test domain thuần, dễ đổi persistence sau.
- **Nhược điểm:** Nhiều boilerplate (mapper, class trùng lặp), tốn công bảo trì. Chỉ nên dùng khi thật sự cần tách domain hoàn toàn (team lớn, nhiều bounded context, hoặc yêu cầu compliance rất chặt).

### Gợi ý

- **Alps (POC / enterprise nhẹ):** giữ **Pragmatic** (domain entity = JPA entity). Đủ dùng, đỡ cực.
- **Khi nào cân nhắc Purist:** Khi domain phức tạp, nhiều rule thuần, cần test domain không cần DB; hoặc chuẩn nội bộ bắt domain không được phụ thuộc JPA. Lúc đó thêm JpaEntity + MapStruct trong infrastructure.persistence và port.out chỉ làm việc với domain entity.

---

## So sánh nhanh (layered vs Clean)

| Layered (YouTube) | Clean / Hexagonal (này) |
|-------------------|-------------------------|
| controller → service → repository | web → **port.in** (use case) → **port.out** (interface) ← **persistence** (adapter) |
| Service gọi repo trực tiếp | Use case gọi **interface** (port.out), không biết JPA |
| DTO thường trong api/dto | DTO trong **application.dto.request** / **application.dto.response** |
| Exception đủ kiểu trong service | **application.exception**; handler trong infrastructure map ra HTTP |

## Luồng phụ thuộc

- **domain**: không phụ thuộc gì (chỉ entity, enum).
- **application**: phụ thuộc **domain**; có **dto** (request/response), **exception**, **port.in/out**, **service**.
- **infrastructure**: phụ thuộc **application** (implement port, dùng DTO/exception) và **domain** (entity); có **GlobalExceptionHandler** map exception → HTTP.

## Gợi ý đặt tên

- **dto.request**: `SubmitLoanRequest`, `GetLoanApplicationQueryRequest`, …
- **dto.response**: `SubmitLoanResponse`, `LoanApplicationSummaryResponse`, …
- **exception**: `AlpsException`, `NotFoundException`, (domain: `InvalidAmountException` nếu để trong domain.exception).
- **port.in**: `SubmitLoanApplicationUseCase`, …
- **port.out**: `LoanApplicationPersistencePort`, `WorkflowStartProcessPort`, …
- **service**: `SubmitLoanApplicationService`, …
