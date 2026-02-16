# Hướng dẫn test API bằng Postman

- App chạy: **http://localhost:8080** (mặc định)
- MySQL (+ tuỳ chọn Zeebe) đã chạy

---

## Cách 1: Tạo dữ liệu qua API (UUIDv7, khuyến nghị)

Tạo lần lượt User → Customer → Loan product qua API, lấy `id` (UUIDv7) từ response rồi dùng khi submit loan.

### 1.1. Tạo User — `POST /api/users`

| Method | URL | Body (raw JSON) |
|--------|-----|------------------|
| POST | `http://localhost:8080/api/users` | Xem bên dưới |

**Headers:** `Content-Type: application/json`

```json
{
  "username": "postman",
  "displayName": "Postman User",
  "email": "postman@test.com",
  "active": true
}
```

**Response 201:** `{ "id": "019xxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx" }` → copy `id` làm **userId**.

---

### 1.2. Tạo Customer — `POST /api/customers`

| Method | URL | Body (raw JSON) |
|--------|-----|------------------|
| POST | `http://localhost:8080/api/customers` | Xem bên dưới |

```json
{
  "civilId": "CID001",
  "fullName": "Nguyen Van A",
  "email": "a@test.com",
  "phoneNumber": "0901234567",
  "monthlyIncome": 20000000,
  "creditScore": 700,
  "employmentStatus": "SALARIED",
  "age": 30
}
```

**Bắt buộc:** `civilId`, `fullName`. Các field còn lại có thể bỏ hoặc `null`.  
**employmentStatus:** một trong `SALARIED`, `SELF_EMPLOYED`, `UNEMPLOYED`, `CONTRACT`, `OTHER`.

**Response 201:** `{ "id": "019xxxxx-..." }` → copy làm **customerId**.

---

### 1.3. Tạo Loan product — `POST /api/loan-products`

| Method | URL | Body (raw JSON) |
|--------|-----|------------------|
| POST | `http://localhost:8080/api/loan-products` | Xem bên dưới |

```json
{
  "code": "PLOAN01",
  "name": "Personal Loan",
  "minAmount": 10000000,
  "maxAmount": 500000000,
  "minTermMonths": 6,
  "maxTermMonths": 60,
  "interestRateAnnual": 12.5,
  "active": true
}
```

**Response 201:** `{ "id": "019xxxxx-..." }` → copy làm **productId**.

---

### 1.4. Submit loan application — `POST /api/loan-applications`

Dùng **customerId**, **productId**, **userId** vừa tạo (UUIDv7).

| Method | URL | Body (raw JSON) |
|--------|-----|------------------|
| POST | `http://localhost:8080/api/loan-applications` | Xem bên dưới |

```json
{
  "customerId": "<customerId từ 1.2>",
  "productId": "<productId từ 1.3>",
  "amount": 50000000,
  "termMonths": 24,
  "submittedByUserId": "<userId từ 1.1>"
}
```

**submittedByUserId** có thể bỏ hoặc `null`.  
**Response 200:** `{ "id": "019xxxxx-..." }` — id loan application (UUIDv7).

---

## Cách 2: Insert SQL (UUID tùy ý, không phải UUIDv7)

Nếu không dùng API tạo dữ liệu, chạy script trong DB `alps`:

```sql
INSERT INTO users (id, username, display_name, active) VALUES
('11111111-1111-1111-1111-111111111111', 'postman', 'Postman User', 1);

INSERT INTO customers (id, civil_id, full_name, email, monthly_income, credit_score, employment_status, age) VALUES
('22222222-2222-2222-2222-222222222222', 'CID001', 'Nguyen Van A', 'a@test.com', 20000000, 700, 'SALARIED', 30);

INSERT INTO loan_products (id, code, name, min_amount, max_amount, min_term_months, max_term_months, interest_rate_annual, active) VALUES
('33333333-3333-3333-3333-333333333333', 'PLOAN01', 'Personal Loan', 10000000, 500000000, 6, 60, 12.5, 1);
```

Sau đó gọi `POST /api/loan-applications` với `customerId`, `productId`, `submittedByUserId` tương ứng. Lưu ý: id này không theo chuẩn UUIDv7.

---

## Tóm tắt endpoint

| Method | URL | Mô tả |
|--------|-----|--------|
| POST | `/api/users` | Tạo user → trả về `id` (UUIDv7) |
| POST | `/api/customers` | Tạo customer → trả về `id` (UUIDv7) |
| POST | `/api/loan-products` | Tạo loan product → trả về `id` (UUIDv7) |
| POST | `/api/loan-applications` | Submit hồ sơ (dùng id từ 3 endpoint trên) |

---

## Lỗi thường gặp

| Mã / Hiện tượng | Nguyên nhân |
|------------------|-------------|
| 404 "Customer not found" / "Product not found" | UUID không tồn tại → tạo qua API (Cách 1) hoặc kiểm tra SQL. |
| 400 validation | Thiếu field bắt buộc, sai kiểu (amount, termMonths, employmentStatus). |
| 409 / duplicate | `civilId`, `code` (loan product), `username` (user) trùng bản ghi đã có. |
