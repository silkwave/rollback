// Banking System JavaScript
console.log("[TRACE] script.js 로딩 시작");

// ms 밀리초만큼 대기하는 함수
const sleep = (ms) => new Promise(resolve => setTimeout(resolve, ms));

class BankingSystem {
  constructor() {
    console.log("[TRACE] BankingSystem constructor 시작");
    this.API_BASE = "/api/banking";
    this.logs = [];
    console.log("[TRACE] API_BASE 설정:", this.API_BASE);
    this.init();
    console.log("[TRACE] BankingSystem constructor 완료");
  }

  init() {
    console.log("[TRACE] init() 메서드 시작");
    this.setupEventListeners();
    this.setupTabs();
    this.loadInitialData();
    console.log("[TRACE] init() 메서드 완료");
  }

  setupEventListeners() {
    console.log("[TRACE] setupEventListeners() 시작");

    // Form submissions
    const depositForm = document.getElementById("depositForm");
    console.log("[TRACE] depositForm 요소:", depositForm ? "찾음" : "없음");
    depositForm?.addEventListener("submit", (e) => {
      console.log("[TRACE] depositForm submit 이벤트 발생");
      e.preventDefault();
      this.processDeposit();
    });

    const customerForm = document.getElementById("customerForm");
    console.log("[TRACE] customerForm 요소:", customerForm ? "찾음" : "없음");
    customerForm?.addEventListener("submit", (e) => {
      console.log("[TRACE] customerForm submit 이벤트 발생");
      e.preventDefault();
      this.createCustomer();
    });

    // Refresh buttons
    const refreshAccountsBtn = document.getElementById("refreshAccountsBtn");
    console.log(
      "[TRACE] refreshAccountsBtn 요소:",
      refreshAccountsBtn ? "찾음" : "없음",
    );
    refreshAccountsBtn?.addEventListener("click", () => {
      console.log("[TRACE] refreshAccountsBtn 클릭");
      this.loadAccounts();
    });

    const refreshCustomersBtn = document.getElementById("refreshCustomersBtn");
    console.log(
      "[TRACE] refreshCustomersBtn 요소:",
      refreshCustomersBtn ? "찾음" : "없음",
    );
    refreshCustomersBtn?.addEventListener("click", () => {
      console.log("[TRACE] refreshCustomersBtn 클릭");
      this.loadCustomers();
    });

    const refreshTransactionsBtn = document.getElementById(
      "refreshTransactionsBtn",
    );
    console.log(
      "[TRACE] refreshTransactionsBtn 요소:",
      refreshTransactionsBtn ? "찾음" : "없음",
    );
    refreshTransactionsBtn?.addEventListener("click", () => {
      console.log("[TRACE] refreshTransactionsBtn 클릭");
      this.loadTransactions();
    });

    const refreshNotificationsBtn = document.getElementById(
      "refreshNotificationsBtn",
    );
    console.log(
      "[TRACE] refreshNotificationsBtn 요소:",
      refreshNotificationsBtn ? "찾음" : "없음",
    );
    refreshNotificationsBtn?.addEventListener("click", () => {
      console.log("[TRACE] refreshNotificationsBtn 클릭");
      this.loadNotifications();
    });

    // Clear logs
    const clearLogsBtn = document.getElementById("clearLogsBtn");
    console.log("[TRACE] clearLogsBtn 요소:", clearLogsBtn ? "찾음" : "없음");
    clearLogsBtn?.addEventListener("click", () => {
      console.log("[TRACE] clearLogsBtn 클릭");
      this.clearLogs();
    });

    console.log("[TRACE] setupEventListeners() 완료");
  }

  setupTabs() {
    console.log("[TRACE] setupTabs() 시작");
    const tabs = document.querySelectorAll(".tab-btn");
    const tabContents = document.querySelectorAll(".tab-content");
    console.log("[TRACE] 탭 버튼 개수:", tabs.length);
    console.log("[TRACE] 탭 컨텐츠 개수:", tabContents.length);

    tabs.forEach((tab, index) => {
      tab.addEventListener("click", () => {
        const targetTab = tab.getAttribute("data-tab");
        console.log(`[TRACE] 탭 클릭: ${targetTab} (인덱스: ${index})`);

        // Update active states with animation
        tabs.forEach((t) => t.classList.remove("active"));
        tabContents.forEach((content) => content.classList.remove("active"));

        // Activate selected tab
        setTimeout(() => {
          tab.classList.add("active");
          document.getElementById(targetTab + "-tab").classList.add("active");
          console.log("[TRACE] 탭 활성화:", targetTab);

          // Load data for active tab
          this.loadTabData(targetTab);
        }, 100);
      });
    });
    console.log("[TRACE] setupTabs() 완료");
  }

  loadTabData(tabName) {
    console.log("[TRACE] loadTabData() 호출:", tabName);
    switch (tabName) {
      case "accounts":
        console.log("[TRACE] 계좌 탭 데이터 로딩");
        this.loadAccounts();
        break;
      case "customers":
        console.log("[TRACE] 고객 탭 데이터 로딩");
        this.loadCustomers();
        break;
      case "transactions":
        console.log("[TRACE] 거래내역 탭 데이터 로딩");
        this.loadTransactions();
        break;
      case "notifications":
        console.log("[TRACE] 알림 로그 탭 데이터 로딩");
        this.loadNotifications();
        break;
      default:
        console.log("[TRACE] 알 수 없는 탭:", tabName);
    }
  }

  async loadInitialData() {
    console.log("[TRACE] loadInitialData() 시작");
    this.addLog("🏦 은행 시스템 초기화 중...", "info");
    try {
      console.log("[TRACE] 초기 데이터 로딩 시작 - Promise.all");
      await Promise.all([
        this.loadAccounts(),
        this.loadCustomers(),
        this.loadTransactions(),
      ]);
      console.log("[TRACE] 초기 데이터 로딩 완료");
      this.addLog("✅ 시스템 초기화 완료", "success");
    } catch (error) {
      console.error("[TRACE] 초기 데이터 로딩 실패:", error);
      this.addLog("❌ 시스템 초기화 실패: " + error.message, "error");
    }
    console.log("[TRACE] loadInitialData() 완료");
  }

  formatCurrency(amount, currency = "KRW") {
    console.log("[TRACE] formatCurrency() 호출:", amount, currency);
    const result = new Intl.NumberFormat("ko-KR", {
      style: "currency",
      currency: currency,
      minimumFractionDigits: 0,
      maximumFractionDigits: 0,
    }).format(amount);
    console.log("[TRACE] formatCurrency() 결과:", result);
    return result;
  }

  formatDate(dateString) {
    console.log("[TRACE] formatDate() 호출:", dateString);
    const date = new Date(dateString);
    const result = date.toLocaleString("ko-KR", {
      year: "numeric",
      month: "2-digit",
      day: "2-digit",
      hour: "2-digit",
      minute: "2-digit",
      second: "2-digit",
    });
    console.log("[TRACE] formatDate() 결과:", result);
    return result;
  }

  getStatusClass(status) {
    console.log("[TRACE] getStatusClass() 호출:", status);
    const statusMap = {
      ACTIVE: "status-active",
      FROZEN: "status-frozen",
      CLOSED: "status-closed",
      SUSPENDED: "status-suspended",
      COMPLETED: "status-completed",
      PENDING: "status-pending",
      FAILED: "status-failed",
    };
    const result = statusMap[status] || "status-pending";
    console.log("[TRACE] getStatusClass() 결과:", result);
    return result;
  }

  getStatusText(status) {
    console.log("[TRACE] getStatusText() 호출:", status);
    const statusMap = {
      ACTIVE: "활성",
      FROZEN: "동결",
      CLOSED: "해지",
      SUSPENDED: "일시정지",
      COMPLETED: "완료",
      PENDING: "처리중",
      FAILED: "실패",
      CANCELLED: "취소",
      DEPOSIT: "입금",
      WITHDRAWAL: "출금",
      CHECKING: "입출금",
      SAVINGS: "적금",
      CREDIT: "신용",
      BUSINESS: "사업자",
      INDIVIDUAL: "개인",
      LOW: "낮음",
      MEDIUM: "보통",
      HIGH: "높음",
    };
    const result = statusMap[status] || status;
    console.log("[TRACE] getStatusText() 결과:", result);
    return result;
  }

  addLog(message, type = "info") {
    console.log(`[TRACE] addLog() 호출: [${type}] ${message}`);
    const logContainer = document.getElementById("logs");
    if (!logContainer) {
      console.error("[TRACE] logContainer 요소를 찾을 수 없음");
      return;
    }

    const logEntry = document.createElement("div");
    logEntry.className = `log-entry log-${type}`;

    const timestamp = new Date().toLocaleTimeString("ko-KR");
    logEntry.innerHTML = `
            <span class="log-time">[${timestamp}]</span>
            <span class="log-message">${message}</span>
        `;

    logContainer.appendChild(logEntry);
    logContainer.scrollTop = logContainer.scrollHeight;

    // Store log
    this.logs.push({
      message,
      type,
      timestamp: new Date().toISOString(),
    });

    // Limit logs to prevent memory issues
    if (this.logs.length > 1000) {
      console.log("[TRACE] 로그 개수 1000개 초과, 오래된 로그 삭제");
      this.logs.shift();
    }
    console.log("[TRACE] addLog() 완료");
  }

  clearLogs() {
    console.log("[TRACE] clearLogs() 호출");
    const logContainer = document.getElementById("logs");
    if (logContainer) {
      logContainer.innerHTML = "";
    }
    this.logs = [];
    this.addLog("🗑️ 로그가 지워졌습니다", "info");
    console.log("[TRACE] clearLogs() 완료");
  }

  showError(message) {
    console.error("[TRACE] showError() 호출:", message);
    this.addLog(`❌ ${message}`, "error");

    // 모달 팝업 표시
    const modal = document.getElementById("errorModal");
    const errorMessage = document.getElementById("errorMessage");
    if (modal && errorMessage) {
      errorMessage.textContent = message;
      modal.classList.add("active");
      console.log("[TRACE] 에러 모달 표시");
    } else {
      // 폴백: 모달이 없으면 alert 사용
      alert(message);
    }
  }

  closeModal() {
    console.log("[TRACE] closeModal() 호출");
    const modal = document.getElementById("errorModal");
    if (modal) {
      modal.classList.remove("active");
      console.log("[TRACE] 모달 닫힘");
    }
  }

  showSuccess(message) {
    console.log("[TRACE] showSuccess() 호출:", message);
    this.addLog(`✅ ${message}`, "success");

    // 토스트 알림 표시
    const toast = document.getElementById("successToast");
    const successMessage = document.getElementById("successMessage");
    if (toast && successMessage) {
      successMessage.textContent = message;
      toast.classList.add("active");
      console.log("[TRACE] 성공 토스트 표시");

      // 3초 후 자동으로 닫기
      setTimeout(() => {
        toast.classList.remove("active");
        console.log("[TRACE] 토스트 자동 닫힘");
      }, 3000);
    }
  }

  async makeRequest(url, options = {}) {
    console.log("[TRACE] makeRequest() 시작:", url, options.method || "GET");
    try {
      console.log("[TRACE] fetch 요청:", url);
      const response = await fetch(url, {
        headers: {
          "Content-Type": "application/json",
          ...options.headers,
        },
        ...options,
      });

      console.log(
        "[TRACE] fetch 응답 상태:",
        response.status,
        response.statusText,
      );

      if (!response.ok) {
        const errorData = await response.json();
        console.error("[TRACE] API 오류 응답:", errorData);
        throw new Error(errorData.message || `HTTP ${response.status}`);
      }

      const result = await response.json();
      console.log("[TRACE] API 응답 데이터:", result);
      return result;
    } catch (error) {
      console.error("[TRACE] makeRequest() 오류:", error);
      this.addLog(`🚨 API 요청 실패: ${error.message}`, "error");
      throw error;
    }
  }

  async loadAccounts() {
    try {
      console.log("[DEBUG] loadAccounts 시작");
      this.addLog("📋 계좌 목록 로딩 중...", "info");

      const response = await fetch(`${this.API_BASE}/accounts`);
      console.log("[DEBUG] API 응답 상태:", response.status);

      if (!response.ok) {
        throw new Error(`HTTP ${response.status}: ${response.statusText}`);
      }

      const accounts = await response.json();
      console.log("[DEBUG] API 응답 데이터:", accounts);

      if (!Array.isArray(accounts)) {
        console.error("[DEBUG] 응답이 배열이 아님:", accounts);
        throw new Error("서버에서 올바른 형식의 데이터를 받지 못했습니다");
      }

      this.renderAccountsTable(accounts);
      this.populateAccountSelects();

      this.addLog(
        `✅ 계좌 목록 로딩 완료 (${accounts.length}개 계좌)`,
        "success",
      );
      console.log("[DEBUG] loadAccounts 완료");
    } catch (error) {
      console.error("[DEBUG] loadAccounts 오류:", error);
      this.showError(`계좌 목록 로딩 실패: ${error.message}`);
    }
  }

  renderAccountsTable(accounts) {
    console.log("[DEBUG] renderAccountsTable 호출, 데이터:", accounts);

    if (!Array.isArray(accounts)) {
      console.error("[DEBUG] accounts가 배열이 아님:", accounts);
      this.showError("계좌 데이터 형식이 올바르지 않습니다");
      return;
    }

    const tbody = document.querySelector("#accountsTable tbody");
    if (!tbody) {
      console.error("[DEBUG] tbody 요소를 찾을 수 없음");
      return;
    }

    tbody.innerHTML = "";
    console.log("[DEBUG] 테이블 초기화 완료, 계좌 수:", accounts.length);

    accounts.forEach((account, index) => {
      console.log(`[DEBUG] 계좌 ${index} 렌더링:`, account);

      if (!account || typeof account !== "object") {
        console.error(`[DEBUG] 계좌 ${index}가 유효하지 않음:`, account);
        return;
      }

      try {
        const row = document.createElement("tr");

        // 상태 값 확인 및 변환
        const status = account.status || "ACTIVE";
        const accountType = account.accountType || "CHECKING";

        row.innerHTML = `
                    <td>${account.id || "-"}</td>
                    <td><strong>${account.accountNumber || "-"}</strong></td>
                    <td>${account.customerId || "-"}</td> <!-- Changed from accountHolderName -->
                    <td><span class="status-badge ${this.getStatusClass(accountType)}">${this.getStatusText(accountType)}</span></td>
                    <td>${account.currency || "KRW"}</td>
                    <td><strong>${this.formatCurrency(account.balance || 0, account.currency || "KRW")}</strong></td>
                    <td><span class="status-badge ${this.getStatusClass(status)}">${this.getStatusText(status)}</span></td>
                    <td>${account.createdAt ? this.formatDate(account.createdAt) : "-"}</td>
                    <td>${this.getAccountActions(account)}</td>
                `;

        tbody.appendChild(row);
        console.log(`[DEBUG] 계좌 ${index} 행 추가 완료`);
      } catch (error) {
        console.error(`[DEBUG] 계좌 ${index} 렌더링 중 오류:`, error);
      }
    });

    console.log("[DEBUG] renderAccountsTable 완료");
  }

  getAccountActions(account) {
    console.log("[DEBUG] getAccountActions 호출:", account);

    let actions = "";
    const status = account.status || "";

    // 상태가 문자열이나 객체일 수 있으므로 toString() 사용
    const statusStr =
      typeof status === "object" && status !== null
        ? status.toString()
        : String(status);

    if (statusStr === "ACTIVE" || statusStr === "active") {
      actions += `<button class="btn-small btn-freeze" onclick="bankingSystem.freezeAccount(${account.id})">동결</button>`;
    } else if (statusStr === "FROZEN" || statusStr === "frozen") {
      actions += `<button class="btn-small btn-activate" onclick="bankingSystem.activateAccount(${account.id})">활성화</button>`;
    }

    console.log("[DEBUG] getAccountActions 결과:", actions);
    return actions;
  }

  async freezeAccount(accountId) {
    console.log("[TRACE] freezeAccount() 호출:", accountId);
    if (!confirm("정말로 계좌를 동결하시겠습니까?")) {
      console.log("[TRACE] 동결 취소 (사용자 확인 거부)");
      return;
    }

    try {
      console.log(
        "[TRACE] 계좌 동결 API 호출:",
        `${this.API_BASE}/accounts/${accountId}/freeze`,
      );
      await this.makeRequest(`${this.API_BASE}/accounts/${accountId}/freeze`, {
        method: "POST",
      });

      console.log("[TRACE] 계좌 동결 성공");
      this.showSuccess("계좌가 동결되었습니다");
      this.loadAccounts();
    } catch (error) {
      console.error("[TRACE] freezeAccount() 오류:", error);
      this.showError(`계좌 동결 실패: ${error.message}`);
    }
  }

  async activateAccount(accountId) {
    console.log("[TRACE] activateAccount() 호출:", accountId);
    if (!confirm("정말로 계좌를 활성화하시겠습니까?")) {
      console.log("[TRACE] 활성화 취소 (사용자 확인 거부)");
      return;
    }

    try {
      console.log(
        "[TRACE] 계좌 활성화 API 호출:",
        `${this.API_BASE}/accounts/${accountId}/activate`,
      );
      await this.makeRequest(
        `${this.API_BASE}/accounts/${accountId}/activate`,
        {
          method: "POST",
        },
      );

      console.log("[TRACE] 계좌 활성화 성공");
      this.showSuccess("계좌가 활성화되었습니다");
      this.loadAccounts();
    } catch (error) {
      console.error("[TRACE] activateAccount() 오류:", error);
      this.showError(`계좌 활성화 실패: ${error.message}`);
    }
  }

  // Transaction Methods
  async processDeposit() {
    console.log("[TRACE] processDeposit() 시작");
    try {
      const form = document.getElementById("depositForm");
      const formData = new FormData(form);
      const data = Object.fromEntries(formData.entries());

      // Convert string values to appropriate numeric types
      data.accountId = parseInt(data.accountId);
      data.customerId = parseInt(data.customerId);
      data.amount = parseFloat(data.amount);
      data.forceFailure = formData.has("forceFailure");

      console.log("[TRACE] 입금 데이터:", data);
      this.addLog(
        `💰 입금 처리 - 계좌ID: ${data.accountId}, 금액: ${this.formatCurrency(data.amount)}`,
        "info",
      );

      console.log("[TRACE] 입금 API 호출:", `${this.API_BASE}/deposit`);
      const result = await this.makeRequest(`${this.API_BASE}/deposit`, {
        method: "POST",
        body: JSON.stringify(data),
      });

      console.log("[TRACE] 입금 성공:", result);
      this.showSuccess(`입금 완료: ${this.formatCurrency(data.amount)}`);
// 1초(1000ms) 대기
    await sleep(1000);      
      form.reset();
// 1초(1000ms) 대기
    await sleep(1000);      
      this.loadAccounts();
// 1초(1000ms) 대기
    await sleep(1000);      
      this.loadTransactions();
    } catch (error) {
      console.error("[TRACE] processDeposit() 오류:", error);
      this.showError(`입금 실패: ${error.message}`);
    }
    console.log("[TRACE] processDeposit() 완료");
  }

  async loadTransactions() {
    console.log("[TRACE] loadTransactions() 시작");
    try {
      this.addLog("📜 거래 내역 로딩 중...", "info");

      console.log(
        "[TRACE] 거래 내역 API 호출:",
        `${this.API_BASE}/transactions`,
      );
      const transactions = await this.makeRequest(
        `${this.API_BASE}/transactions`,
      );
      console.log("[TRACE] 거래 내역 수신:", transactions.length, "개");

      this.renderTransactionsTable(transactions);

      this.addLog(
        `✅ 거래 내역 로딩 완료 (${transactions.length}개 거래)`,
        "success",
      );
    } catch (error) {
      console.error("[TRACE] loadTransactions() 오류:", error);
      this.showError(`거래 내역 로딩 실패: ${error.message}`);
    }
    console.log("[TRACE] loadTransactions() 완료");
  }

  renderTransactionsTable(transactions) {
    console.log(
      "[TRACE] renderTransactionsTable() 시작:",
      transactions.length,
      "개 거래",
    );
    const tbody = document.querySelector("#transactionsTable tbody");
    if (!tbody) {
      console.error("[TRACE] transactionsTable tbody 요소를 찾을 수 없음");
      return;
    }

    tbody.innerHTML = "";
    console.log("[TRACE] 거래 테이블 초기화 완료");

    transactions.forEach((transaction, index) => {
      console.log(
        `[TRACE] 거래 ${index + 1} 렌더링:`,
        transaction.id,
        transaction.guid,
      );
      const row = document.createElement("tr");
      row.innerHTML = `
                <td>${transaction.id}</td>
                <td><strong>${transaction.guid}</strong></td>
                <td>${transaction.fromAccountId || "-"}</td>
                <td>${transaction.toAccountId || "-"}</td>
                <td><span class="status-badge ${this.getStatusClass(transaction.transactionType)}">${this.getStatusText(transaction.transactionType)}</span></td>
                <td><strong>${this.formatCurrency(transaction.amount, transaction.currency)}</strong></td>
                <td>${transaction.currency}</td>
                <td><span class="status-badge ${this.getStatusClass(transaction.status)}">${this.getStatusText(transaction.status)}</span></td>
                <td>${transaction.description || "-"}</td>
                <td>${this.formatDate(transaction.createdAt)}</td>
            `;
      tbody.appendChild(row);
    });
    console.log("[TRACE] renderTransactionsTable() 완료");
  }

  async loadNotifications() {
    console.log("[TRACE] loadNotifications() 시작");
    try {
      this.addLog("🔔 알림 로그 로딩 중...", "info");

      console.log(
        "[TRACE] 알림 로그 API 호출:",
        `${this.API_BASE}/notifications`,
      );
      const notifications = await this.makeRequest(
        `${this.API_BASE}/notifications`,
      );
      console.log("[TRACE] 알림 로그 수신:", notifications.length, "개");

      this.renderNotificationsTable(notifications);

      this.addLog(
        `✅ 알림 로그 로딩 완료 (${notifications.length}개 알림)`,
        "success",
      );
    } catch (error) {
      console.error("[TRACE] loadNotifications() 오류:", error);
      this.showError(`알림 로그 로딩 실패: ${error.message}`);
    }
    console.log("[TRACE] loadNotifications() 완료");
  }

  renderNotificationsTable(notifications) {
    console.log(
      "[TRACE] renderNotificationsTable() 시작:",
      notifications.length,
      "개 알림",
    );
    const tbody = document.querySelector("#notificationsTable tbody");
    if (!tbody) {
      console.error("[TRACE] notificationsTable tbody 요소를 찾을 수 없음");
      return;
    }

    tbody.innerHTML = "";
    console.log("[TRACE] 알림 로그 테이블 초기화 완료");

    notifications.forEach((notification, index) => {
      console.log(
        `[TRACE] 알림 ${index + 1} 렌더링:`,
        notification.id,
        notification.guid,
      );
      const row = document.createElement("tr");
      row.innerHTML = `
                <td>${notification.id}</td>
                <td><strong>${notification.guid || "-"}</strong></td>
                <td>${notification.message || "-"}</td>
                <td><span class="status-badge ${this.getNotificationStatusClass(notification.type)}">${this.getNotificationStatusText(notification.type)}</span></td>
                <td>${this.formatDate(notification.createdAt)}</td>
            `;
      tbody.appendChild(row);
    });
    console.log("[TRACE] renderNotificationsTable() 완료");
  }

  getNotificationStatusClass(type) {
    const statusMap = {
      SUCCESS: "status-success",
      FAILURE: "status-failure",
      INFO: "status-info",
      WARNING: "status-warning",
    };
    return statusMap[type] || "status-info";
  }

  getNotificationStatusText(type) {
    const statusMap = {
      SUCCESS: "성공",
      FAILURE: "실패",
      INFO: "정보",
      WARNING: "경고",
    };
    return statusMap[type] || type;
  }

  // Customer Methods
  async createCustomer() {
    console.log("[TRACE] createCustomer() 시작");
    try {
      const form = document.getElementById("customerForm");
      const formData = new FormData(form);
      const data = Object.fromEntries(formData.entries());

      console.log("[TRACE] 고객 등록 데이터:", data);
      this.addLog(
        `👤 고객 등록 요청 - ${data.name}`,
        "info",
      );

      console.log("[TRACE] 고객 등록 API 호출:", `${this.API_BASE}/customers`);
      const result = await this.makeRequest(`${this.API_BASE}/customers`, {
        method: "POST",
        body: JSON.stringify(data),
      });

      console.log("[TRACE] 고객 등록 성공:", result);
      this.showSuccess(`고객 등록 완료: ${result.customerNumber}`);
      form.reset();
      this.loadCustomers();
    } catch (error) {
      console.error("[TRACE] createCustomer() 오류:", error);
      this.showError(`고객 등록 실패: ${error.message}`);
    }
    console.log("[TRACE] createCustomer() 완료");
  }

  async loadCustomers() {
    console.log("[TRACE] loadCustomers() 시작");
    try {
      this.addLog("👥 고객 목록 로딩 중...", "info");

      console.log("[TRACE] 고객 목록 API 호출:", `${this.API_BASE}/customers`);
      const customers = await this.makeRequest(`${this.API_BASE}/customers`);
      console.log("[TRACE] 고객 목록 수신:", customers.length, "명");

      this.renderCustomersTable(customers);

      this.addLog(`✅ 고객 목록 로딩 완료 (${customers.length}명)`, "success");
    } catch (error) {
      console.error("[TRACE] loadCustomers() 오류:", error);
      this.showError(`고객 목록 로딩 실패: ${error.message}`);
    }
    console.log("[TRACE] loadCustomers() 완료");
  }

  renderCustomersTable(customers) {
    console.log(
      "[TRACE] renderCustomersTable() 시작:",
      customers.length,
      "명 고객",
    );
    const tbody = document.querySelector("#customersTable tbody");
    if (!tbody) {
      console.error("[TRACE] customersTable tbody 요소를 찾을 수 없음");
      return;
    }

    tbody.innerHTML = "";
    console.log("[TRACE] 고객 테이블 초기화 완료");

    customers.forEach((customer, index) => {
      console.log(
        `[TRACE] 고객 ${index + 1} 렌더링:`,
        customer.id,
        customer.customerNumber,
      );
      const row = document.createElement("tr");
      row.innerHTML = `
                <td>${customer.id}</td>
                <td><strong>${customer.customerNumber}</strong></td>
                <td>${customer.name}</td>
                <td>${customer.email}</td>
                <td>${customer.phoneNumber}</td>
                <td><span class="status-badge ${this.getStatusClass(customer.status)}">${this.getStatusText(customer.status)}</span></td>
                <td>${this.formatDate(customer.createdAt)}</td>
                <td>
                    <button class="btn-small btn-cancel" onclick="bankingSystem.suspendCustomer(${customer.id})">정지</button>
                </td>
            `;
      tbody.appendChild(row);
    });
    console.log("[TRACE] renderCustomersTable() 완료");
  }

  async suspendCustomer(customerId) {
    console.log("[TRACE] suspendCustomer() 호출:", customerId);
    if (!confirm("정말로 고객을 정지하시겠습니까?")) {
      console.log("[TRACE] 고객 정지 취소 (사용자 확인 거부)");
      return;
    }

    try {
      console.log(
        "[TRACE] 고객 정지 API 호출:",
        `${this.API_BASE}/customers/${customerId}/suspend`,
      );
      await this.makeRequest(
        `${this.API_BASE}/customers/${customerId}/suspend`,
        {
          method: "POST",
        },
      );

      console.log("[TRACE] 고객 정지 성공");
      this.showSuccess("고객이 정지되었습니다");
      this.loadCustomers();
    } catch (error) {
      console.error("[TRACE] suspendCustomer() 오류:", error);
      this.showError(`고객 정지 실패: ${error.message}`);
    }
  }

  // Utility Methods
  populateAccountSelects() {
    console.log("[DEBUG] populateAccountSelects 시작");

    const selects = ["depositAccountId", "withdrawAccountId"];

    selects.forEach((selectId) => {
      const select = document.getElementById(selectId);
      if (!select) {
        console.log(`[DEBUG] ${selectId} 요소 없음, 건너뜀`);
        return;
      }

      // Get current accounts from table
      const tbody = document.querySelector("#accountsTable tbody");
      if (!tbody) {
        console.error("[DEBUG] accountsTable tbody 요소를 찾을 수 없음");
        return;
      }

      const rows = tbody.querySelectorAll("tr");
      console.log(`[DEBUG] 테이블에서 ${rows.length}개 행 발견`);

      const accounts = Array.from(rows)
        .map((row, index) => {
          const cells = row.querySelectorAll("td");
          if (cells.length < 9) {
            console.warn(
              `[DEBUG] 행 ${index}에 셀이 부족함: ${cells.length}개`,
            );
            return null;
          }
          return {
            id: cells[0]?.textContent || "",
            accountNumber: cells[1]?.textContent?.trim() || "",
            status: cells[6]?.textContent?.trim() || "",
          };
        })
        .filter((acc) => acc !== null);

      console.log(
        `[DEBUG] ${selectId}용 계좌 ${accounts.length}개 추출`,
        accounts,
      );

      // Save the first option if it exists
      const firstOption = select.querySelector("option");
      const firstOptionClone = firstOption ? firstOption.cloneNode(true) : null;

      // Clear all options
      select.innerHTML = "";

      // Add the first option back if it existed
      if (firstOptionClone) {
        select.appendChild(firstOptionClone);
        console.log(
          `[DEBUG] 첫 번째 옵션 추가: ${firstOptionClone.textContent}`,
        );
      } else {
        // Create default placeholder if no first option
        const defaultOption = document.createElement("option");
        defaultOption.value = "";
        defaultOption.textContent = "계좌 선택";
        defaultOption.disabled = true;
        defaultOption.selected = true;
        select.appendChild(defaultOption);
        console.log(`[DEBUG] 기본 옵션 생성`);
      }

      // Add account options
      let addedCount = 0;
      accounts.forEach((account) => {
        const statusText = account.status || "";
        if (statusText.includes("활성") || statusText.includes("ACTIVE")) {
          const option = document.createElement("option");
          option.value = account.id;
          option.textContent = account.accountNumber || `계좌 ${account.id}`;
          select.appendChild(option);
          addedCount++;
        }
      });

      console.log(`[DEBUG] ${selectId}에 활성 계좌 ${addedCount}개 추가`);
    });

    console.log("[DEBUG] populateAccountSelects 완료");
  }
}

// Initialize banking system when DOM is loaded
console.log("[TRACE] DOMContentLoaded 리스너 등록");
document.addEventListener("DOMContentLoaded", function () {
  console.log(
    "[TRACE] DOMContentLoaded 콜백 실행 - BankingSystem 인스턴스 생성 시작",
  );
  try {
    window.bankingSystem = new BankingSystem();
    window.bankingApp = window.bankingSystem; // HTML onclick 핸들러용 별칭
    console.log(
      "[TRACE] BankingSystem 인스턴스 생성 완료, window.bankingSystem:",
      window.bankingSystem,
    );
  } catch (error) {
    console.error("[TRACE] BankingSystem 인스턴스 생성 오류:", error);
  }
});

console.log("[TRACE] script.js 로딩 완료");
