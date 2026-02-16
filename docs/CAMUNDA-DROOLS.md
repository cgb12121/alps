# Luồng test Camunda + Drools

## Cần chạy app riêng (Zeebe) không?

**Có.** Để chạy **đầy đủ** luồng (submit hồ sơ → Camunda start process → worker gọi Drools → cập nhật trạng thái), bạn cần **Zeebe** (Camunda 8) chạy trước. Ứng dụng Spring Boot kết nối tới Zeebe qua gRPC (mặc định `localhost:26500`) hoặc REST.

### Chạy Zeebe nhanh (Docker)

```bash
docker run -d -p 26500:26500 camunda/zeebe:8.5.0
```

Sau đó chạy ứng dụng Alps; khi submit loan application, process sẽ được start và worker "score-loan" sẽ chạy Drools rồi cập nhật `LoanApplication.status` và tạo `RiskAssessment`.

### Cấu hình (application.properties)

```properties
# Mặc định starter kết nối localhost:26500 (gRPC). Nếu dùng REST:
# camunda.client.zeebe.gateway-address=http://localhost:8080
```

Tắt deploy BPMN lúc startup (nếu bạn deploy bằng Modeler):

```properties
camunda.workflow.deploy-on-startup=false
```

---

## Luồng đơn giản (Submit → Score → End)

1. **Submit:** `POST /api/loan-applications` → `SubmitLoanApplicationUseCase` tạo `LoanApplication` (status `SUBMITTED`), gọi `StartProcessPort.startProcess("loan-approval", { applicationId })`, lưu `processInstanceKey` vào `LoanApplication`.
2. **BPMN:** Process `loan-approval`: Start → **Check CIC** (giả lập) → **Score loan (Drools)** → **Gateway (Phê duyệt?)** → APPROVED / REJECTED / **REVIEW_REQUIRED** (chờ message "approvalDecision"). Nhánh REVIEW_REQUIRED: **Message Catch Event** (chờ duyệt tay) → Gateway (approved?) → End Duyệt tay: Chấp thuận / Từ chối.
3. **Workers:** `CheckCicJobWorker` (check-cic): giả lập CIC, trả `cicClean`, `cicReportId`. `ScoreLoanJobWorker` (score-loan): như trên, trả `decision`.
4. **Drools:** `ScoreLoanService` load application + customer, tạo `LoanScoringFact`, gọi `RiskScoringPort.score(fact)` (adapter chạy DRL `drools/loan-scoring.drl`), lưu `RiskAssessment`, cập nhật `LoanApplication.status`, và trả về `LoanStatus` cho worker ghi vào process.
5. Gateway đánh giá `decision` → process kết thúc (Approved/Rejected) hoặc chờ **duyệt tay** (message "approvalDecision" với `approved` true/false). Khi có quyết định cuối (APPROVED/REJECTED), **notification** (email/Slack) được gửi nếu cấu hình.

**Submit không chờ process:** API `POST /api/loan-applications` trả về **ngay** (201 Created) với `id` và `status: SUBMITTED`. Việc start Camunda process chạy **async** (sau commit, trên virtual thread); user không phải đợi Zeebe hay duyệt tay. Client có thể poll `GET /api/loan-applications/{id}` để xem khi nào `status` chuyển sang APPROVED / REJECTED / REVIEW_REQUIRED.

---

## Test không cần Zeebe

- **Drools thuần:** `DroolsRiskScoringAdapterTest` – test rules với nhiều input (auto approve, auto reject, manual check). Chạy: `mvn test -Dtest=DroolsRiskScoringAdapterTest`.
- **Full context:** Nếu không chạy Zeebe, context Spring có thể lỗi khi kết nối. Để test tích hợp (DB + Drools, không Camunda), dùng profile test và mock `StartProcessPort` / hoặc tắt auto-config Camunda client trong test.

---

## Tóm tắt

| Thành phần | Vai trò |
|------------|--------|
| **Zeebe (Docker)** | Broker process; **cần chạy riêng** để luồng end-to-end hoạt động. |
| **BPMN** | `src/main/resources/bpmn/loan-approval.bpmn` – process 1 task `score-loan`. |
| **Drools** | `src/main/resources/drools/loan-scoring.drl` + `RiskScoringPort` → AUTO_APPROVE / AUTO_REJECT / MANUAL_CHECK. |
| **Worker** | `CheckCicJobWorker` (check-cic), `ScoreLoanJobWorker` (score-loan). |

Không cài app GUI (Modeler, Operate) vẫn chạy được: BPMN được deploy từ code lúc start process lần đầu.

**Camunda 8.8 & annotation:** Worker dùng `@JobWorker(type = "score-loan")` trong `ScoreLoanJobWorker` (package `io.camunda.client.annotation`). Deploy BPMN vẫn thủ công trong `ZeebeStartProcessAdapter`.

---

## Luồng chạy thành công (log mẫu)

Khi thấy log tương tự sau là **luồng đã chạy đầy đủ**:

- `Activated 1 jobs for worker default and job type score-loan`
- `Processing score-loan job for applicationId=...`
- `Drools: scoring fact amount=..., creditScore=..., firing rules`
- `Drools: result decision=AUTO_APPROVE, score=85, reasons=...`
- `Score-loan job completed for applicationId=...`

→ Process instance đã chạy, worker đã xử lý job, Drools đã chấm điểm và cập nhật `LoanApplication.status` + `RiskAssessment`.

---

## Dashboard Camunda (Operate)

**Operate** là giao diện web để xem process instances, flow, jobs, incidents.

Compose hiện tại chỉ có **Zeebe** (broker). Operate cần thêm **Elasticsearch** (Zeebe export dữ liệu sang ES, Operate đọc từ ES).

### Cách 1: Dùng Docker Compose chính thức Camunda 8 (khuyến nghị)

Tải bộ docker-compose đầy đủ (Zeebe + Elasticsearch + Operate + Tasklist):

- **Link:** [Developer quickstart with Docker Compose](https://docs.camunda.io/docs/self-managed/setup/deploy/local/docker-compose/)
- Chạy stack đó thay vì chỉ `zeebe` đơn lẻ; app Alps vẫn trỏ `camunda.client.grpc-address` tới Zeebe (port 26500).

Sau khi stack chạy:

- **Operate:** http://localhost:8088/operate (đăng nhập mặc định: `demo` / `demo`)
- Trong Operate: vào **Processes** → chọn process **loan-approval** → xem **Process instances** và từng instance (variables, flow, jobs).

### Cách 2: Giữ compose hiện tại (chỉ Zeebe)

Không chạy Operate thì vẫn deploy/start process và worker bình thường; chỉ không có giao diện xem process instance. Kiểm tra qua log app hoặc DB (`loan_applications.process_instance_key`, `risk_assessments`, `loan_applications.status`).

---

## Duyệt tay (Human Task)

Khi Drools trả **MANUAL_CHECK** (điểm lửng lơ), process chuyển status **REVIEW_REQUIRED** và dừng tại **Message Catch Event** chờ API gửi quyết định.

- **GET /api/loan-applications/pending-approval** – danh sách hồ sơ chờ duyệt (status = REVIEW_REQUIRED).
- **POST /api/loan-applications/{id}/approve** – chấp thuận (body optional: `{ "reviewedByUserId": "uuid" }`).
- **POST /api/loan-applications/{id}/reject** – từ chối.

Backend publish message Zeebe `approvalDecision` với `correlationKey = applicationId`, biến `approved` true/false; process chạy tiếp đến End tương ứng và cập nhật `LoanApplication` (status, reviewedBy, reviewedAt).

---

## CIC (giả lập tích hợp bên thứ 3)

Task **Check CIC** trong BPMN gọi worker `check-cic`. `CICCheckAdapter` giả lập: **Thread.sleep(2000)** (độ trễ mạng) và **random ~20% nợ xấu**. Trả biến process `cicClean`, `cicReportId`. Có thể mở rộng Drools hoặc bước sau dùng `cicClean` để từ chối nếu nợ xấu.

---

## Notification (Email / Slack)

Khi trạng thái hồ sơ chuyển sang **APPROVED** hoặc **REJECTED** (tự động hoặc duyệt tay), event `LoanApplicationDecidedEvent` được publish. Listener gọi tất cả `NotificationPort`:

- **Email:** Bật `alps.notification.mail.enabled=true` và cấu hình `spring.mail.*` (Gmail SMTP, app password).
- **Slack:** Set `alps.notification.slack.webhook-url` = URL Incoming Webhook.

Chi tiết xem comment trong `application.properties`.

---

### Zeebe Simple Monitor (dashboard nhẹ)

**Zeebe Simple Monitor** (port 8082) cần **Hazelcast**. Hazelcast được cung cấp bởi Zeebe broker khi bật **zeebe-hazelcast-exporter**; image `camunda/zeebe` mặc định không có exporter này, nên Simple Monitor sẽ lỗi kết nối `localhost:5701`. Trong `docker-compose.yml`, service `simple-monitor` đã được comment; nếu muốn dùng, cần cấu hình Zeebe với Hazelcast exporter (mount JAR + env) rồi bỏ comment và set `ZEEBE_WORKER_HAZELCAST_CONNECTION=zeebe:5701`. Khuyến nghị: dùng **Operate** (Cách 1) cho dashboard đầy đủ.
