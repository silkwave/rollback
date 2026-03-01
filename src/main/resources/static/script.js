// Banking System JavaScript

// ms 밀리초만큼 대기하는 함수
const sleep = (ms) => new Promise(resolve => setTimeout(resolve, ms));

class BankingSystem {
  constructor() {
    this.API_BASE = "/api/banking";
    this.logs = [];
    this.init();
  }

  init() {
    this.setupEventListeners();
    this.setupTabs();
    this.loadInitialData();
  }

  setupEventListeners() {

    // Form submissions
    const depositForm = document.getElementById("depositForm");
    depositForm?.addEventListener("submit", (e) => {
      e.preventDefault();
      this.processDeposit();
    });

    const customerForm = document.getElementById("customerForm");
    customerForm?.addEventListener("submit", (e) => {
      e.preventDefault();
      this.createCustomer();
    });

    // Refresh buttons
    const refreshAccountsBtn = document.getElementById("refreshAccountsBtn");
    refreshAccountsBtn?.addEventListener("click", () => {
      this.loadAccounts();
    });

    const refreshCustomersBtn = document.getElementById("refreshCustomersBtn");
    refreshCustomersBtn?.addEventListener("click", () => {
      this.loadCustomers();
    });

    const refreshTransactionsBtn = document.getElementById(
      "refreshTransactionsBtn",
    );
    refreshTransactionsBtn?.addEventListener("click", () => {
      this.loadTransactions();
    });

    const refreshNotificationsBtn = document.getElementById(
      "refreshNotificationsBtn",
    );
    refreshNotificationsBtn?.addEventListener("click", () => {
      this.loadNotifications();
    });

    // Clear logs
    const clearLogsBtn = document.getElementById("clearLogsBtn");
    clearLogsBtn?.addEventListener("click", () => {
      this.clearLogs();
    });

  }

  setupTabs() {
    const tabs = document.querySelectorAll(".tab-btn");
    const tabContents = document.querySelectorAll(".tab-content");

    tabs.forEach((tab, index) => {
      tab.addEventListener("click", () => {
        const targetTab = tab.getAttribute("data-tab");

        // Update active states with animation
        tabs.forEach((t) => t.classList.remove("active"));
        tabContents.forEach((content) => content.classList.remove("active"));

        // Activate selected tab
        setTimeout(() => {
          tab.classList.add("active");
          document.getElementById(targetTab + "-tab").classList.add("active");

          // Load data for active tab
          this.loadTabData(targetTab);
        }, 100);
      });
    });
  }

  loadTabData(tabName) {
    switch (tabName) {
      case "accounts":
        this.loadAccounts();
        break;
      case "customers":
        this.loadCustomers();
        break;
      case "transactions":
        this.loadTransactions();
        break;
      case "notifications":
        this.loadNotifications();
        break;
      default:
    }
  }

  async loadInitialData() {
    this.addLog("🏦 은행 시스템 초기화 중...", "info");
    try {
      await Promise.all([
        this.loadAccounts(),
        this.loadCustomers(),
        this.loadTransactions(),
      ]);
      this.addLog("✅ 시스템 초기화 완료", "success");
    } catch (error) {
      console.error("[TRACE] 초기 데이터 로딩 실패:", error);
      this.addLog("❌ 시스템 초기화 실패: " + error.message, "error");
    }
  }

  formatCurrency(amount, currency = "KRW") {
    const result = new Intl.NumberFormat("ko-KR", {
      style: "currency",
      currency: currency,
      minimumFractionDigits: 0,
      maximumFractionDigits: 0,
    }).format(amount);
    return result;
  }

  formatDate(dateString) {
    const date = new Date(dateString);
    const result = date.toLocaleString("ko-KR", {
      year: "numeric",
      month: "2-digit",
      day: "2-digit",
      hour: "2-digit",
      minute: "2-digit",
      second: "2-digit",
    });
    return result;
  }

  getStatusClass(status) {
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
    return result;
  }

  getStatusText(status) {
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
    return result;
  }

  addLog(message, type = "info") {
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
      this.logs.shift();
    }
  }

  clearLogs() {
    const logContainer = document.getElementById("logs");
    if (logContainer) {
      logContainer.innerHTML = "";
    }
    this.logs = [];
    this.addLog("🗑️ 로그가 지워졌습니다", "info");
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
    } else {
      // 폴백: 모달이 없으면 alert 사용
      alert(message);
    }
  }

  closeModal() {
    const modal = document.getElementById("errorModal");
    if (modal) {
      modal.classList.remove("active");
    }
  }

  showSuccess(message) {
    this.addLog(`✅ ${message}`, "success");

    // 토스트 알림 표시
    const toast = document.getElementById("successToast");
    const successMessage = document.getElementById("successMessage");
    if (toast && successMessage) {
      successMessage.textContent = message;
      toast.classList.add("active");

      // 3초 후 자동으로 닫기
      setTimeout(() => {
        toast.classList.remove("active");
      }, 3000);
    }
  }

  async makeRequest(url, options = {}) {
    try {
      const response = await fetch(url, {
        headers: {
          "Content-Type": "application/json",
          ...options.headers,
        },
        ...options,
      });

      if (!response.ok) {
        const errorData = await response.json();
        console.error("[TRACE] API 오류 응답:", errorData);
        throw new Error(errorData.message || `HTTP ${response.status}`);
      }

      const result = await response.json();
      return result;
    } catch (error) {
      console.error("[TRACE] makeRequest() 오류:", error);
      this.addLog(`🚨 API 요청 실패: ${error.message}`, "error");
      throw error;
    }
  }

  async loadAccounts() {
    try {
      this.addLog("📋 계좌 목록 로딩 중...", "info");

      const response = await fetch(`${this.API_BASE}/accounts`);

      if (!response.ok) {
        throw new Error(`HTTP ${response.status}: ${response.statusText}`);
      }

      const accounts = await response.json();

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
    } catch (error) {
      console.error("[DEBUG] loadAccounts 오류:", error);
      this.showError(`계좌 목록 로딩 실패: ${error.message}`);
    }
  }

  renderAccountsTable(accounts) {

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

    accounts.forEach((account, index) => {

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
      } catch (error) {
        console.error(`[DEBUG] 계좌 ${index} 렌더링 중 오류:`, error);
      }
    });

  }

  getAccountActions(account) {

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

    return actions;
  }

  async freezeAccount(accountId) {
    if (!confirm("정말로 계좌를 동결하시겠습니까?")) {
      return;
    }

    try {
      await this.makeRequest(`${this.API_BASE}/accounts/${accountId}/freeze`, {
        method: "POST",
      });

      this.showSuccess("계좌가 동결되었습니다");
      this.loadAccounts();
    } catch (error) {
      console.error("[TRACE] freezeAccount() 오류:", error);
      this.showError(`계좌 동결 실패: ${error.message}`);
    }
  }

  async activateAccount(accountId) {
    if (!confirm("정말로 계좌를 활성화하시겠습니까?")) {
      return;
    }

    try {
      await this.makeRequest(
        `${this.API_BASE}/accounts/${accountId}/activate`,
        {
          method: "POST",
        },
      );

      this.showSuccess("계좌가 활성화되었습니다");
      this.loadAccounts();
    } catch (error) {
      console.error("[TRACE] activateAccount() 오류:", error);
      this.showError(`계좌 활성화 실패: ${error.message}`);
    }
  }

  // Transaction Methods
  async processDeposit() {
    try {
      const form = document.getElementById("depositForm");
      const formData = new FormData(form);
      const data = Object.fromEntries(formData.entries());

      // Convert string values to appropriate numeric types
      data.accountId = parseInt(data.accountId);
      data.customerId = parseInt(data.customerId);
      data.amount = parseFloat(data.amount);
      data.forceFailure = formData.has("forceFailure");

      this.addLog(
        `💰 입금 처리 - 계좌ID: ${data.accountId}, 금액: ${this.formatCurrency(data.amount)}`,
        "info",
      );

      const result = await this.makeRequest(`${this.API_BASE}/deposit`, {
        method: "POST",
        body: JSON.stringify(data),
      });

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
  }

  async loadTransactions() {
    try {
      this.addLog("📜 거래 내역 로딩 중...", "info");

      const transactions = await this.makeRequest(
        `${this.API_BASE}/transactions`,
      );

      this.renderTransactionsTable(transactions);

      this.addLog(
        `✅ 거래 내역 로딩 완료 (${transactions.length}개 거래)`,
        "success",
      );
    } catch (error) {
      console.error("[TRACE] loadTransactions() 오류:", error);
      this.showError(`거래 내역 로딩 실패: ${error.message}`);
    }
  }

  renderTransactionsTable(transactions) {
    const tbody = document.querySelector("#transactionsTable tbody");
    if (!tbody) {
      console.error("[TRACE] transactionsTable tbody 요소를 찾을 수 없음");
      return;
    }

    tbody.innerHTML = "";

    transactions.forEach((transaction, index) => {
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
  }

  async loadNotifications() {
    try {
      this.addLog("🔔 알림 로그 로딩 중...", "info");

      const notifications = await this.makeRequest(
        `${this.API_BASE}/notifications`,
      );

      this.renderNotificationsTable(notifications);

      this.addLog(
        `✅ 알림 로그 로딩 완료 (${notifications.length}개 알림)`,
        "success",
      );
    } catch (error) {
      console.error("[TRACE] loadNotifications() 오류:", error);
      this.showError(`알림 로그 로딩 실패: ${error.message}`);
    }
  }

  renderNotificationsTable(notifications) {
    const tbody = document.querySelector("#notificationsTable tbody");
    if (!tbody) {
      console.error("[TRACE] notificationsTable tbody 요소를 찾을 수 없음");
      return;
    }

    tbody.innerHTML = "";

    notifications.forEach((notification, index) => {
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
    try {
      const form = document.getElementById("customerForm");
      const formData = new FormData(form);
      const data = Object.fromEntries(formData.entries());

      this.addLog(
        `👤 고객 등록 요청 - ${data.name}`,
        "info",
      );

      const result = await this.makeRequest(`${this.API_BASE}/customers`, {
        method: "POST",
        body: JSON.stringify(data),
      });

      this.showSuccess(`고객 등록 완료: ${result.customerNumber}`);
      form.reset();
      this.loadCustomers();
    } catch (error) {
      console.error("[TRACE] createCustomer() 오류:", error);
      this.showError(`고객 등록 실패: ${error.message}`);
    }
  }

  async loadCustomers() {
    try {
      this.addLog("👥 고객 목록 로딩 중...", "info");

      const customers = await this.makeRequest(`${this.API_BASE}/customers`);

      this.renderCustomersTable(customers);

      this.addLog(`✅ 고객 목록 로딩 완료 (${customers.length}명)`, "success");
    } catch (error) {
      console.error("[TRACE] loadCustomers() 오류:", error);
      this.showError(`고객 목록 로딩 실패: ${error.message}`);
    }
  }

  renderCustomersTable(customers) {
    const tbody = document.querySelector("#customersTable tbody");
    if (!tbody) {
      console.error("[TRACE] customersTable tbody 요소를 찾을 수 없음");
      return;
    }

    tbody.innerHTML = "";

    customers.forEach((customer, index) => {
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
  }

  async suspendCustomer(customerId) {
    if (!confirm("정말로 고객을 정지하시겠습니까?")) {
      return;
    }

    try {
      await this.makeRequest(
        `${this.API_BASE}/customers/${customerId}/suspend`,
        {
          method: "POST",
        },
      );

      this.showSuccess("고객이 정지되었습니다");
      this.loadCustomers();
    } catch (error) {
      console.error("[TRACE] suspendCustomer() 오류:", error);
      this.showError(`고객 정지 실패: ${error.message}`);
    }
  }

  // Utility Methods
  populateAccountSelects() {

    const selects = ["depositAccountId", "withdrawAccountId"];

    selects.forEach((selectId) => {
      const select = document.getElementById(selectId);
      if (!select) {
        return;
      }

      // Get current accounts from table
      const tbody = document.querySelector("#accountsTable tbody");
      if (!tbody) {
        console.error("[DEBUG] accountsTable tbody 요소를 찾을 수 없음");
        return;
      }

      const rows = tbody.querySelectorAll("tr");

      const accounts = Array.from(rows)
        .map((row, index) => {
          const cells = row.querySelectorAll("td");
          if (cells.length < 9) {
            return null;
          }
          return {
            id: cells[0]?.textContent || "",
            accountNumber: cells[1]?.textContent?.trim() || "",
            status: cells[6]?.textContent?.trim() || "",
          };
        })
        .filter((acc) => acc !== null);

      // Save the first option if it exists
      const firstOption = select.querySelector("option");
      const firstOptionClone = firstOption ? firstOption.cloneNode(true) : null;

      // Clear all options
      select.innerHTML = "";

      // Add the first option back if it existed
      if (firstOptionClone) {
        select.appendChild(firstOptionClone);
      } else {
        // Create default placeholder if no first option
        const defaultOption = document.createElement("option");
        defaultOption.value = "";
        defaultOption.textContent = "계좌 선택";
        defaultOption.disabled = true;
        defaultOption.selected = true;
        select.appendChild(defaultOption);
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

    });

  }
}

// Initialize banking system when DOM is loaded
document.addEventListener("DOMContentLoaded", function () {
  try {
    window.bankingSystem = new BankingSystem();
    window.bankingApp = window.bankingSystem; // HTML onclick 핸들러용 별칭
  } catch (error) {
    console.error("[TRACE] BankingSystem 인스턴스 생성 오류:", error);
  }
});
