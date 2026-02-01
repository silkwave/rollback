// Banking System JavaScript
console.log('[TRACE] script.js 로딩 시작');

class BankingSystem {
    constructor() {
        console.log('[TRACE] BankingSystem constructor 시작');
        this.API_BASE = '/api/banking';
        this.logs = [];
        console.log('[TRACE] API_BASE 설정:', this.API_BASE);
        this.init();
        console.log('[TRACE] BankingSystem constructor 완료');
    }

    init() {
        console.log('[TRACE] init() 메서드 시작');
        this.setupEventListeners();
        this.setupTabs();
        this.loadInitialData();
        console.log('[TRACE] init() 메서드 완료');
    }

    setupEventListeners() {
        console.log('[TRACE] setupEventListeners() 시작');
        
        // Form submissions
        const accountForm = document.getElementById('accountForm');
        console.log('[TRACE] accountForm 요소:', accountForm ? '찾음' : '없음');
        accountForm?.addEventListener('submit', (e) => {
            console.log('[TRACE] accountForm submit 이벤트 발생');
            e.preventDefault();
            this.createAccount();
        });

        const depositForm = document.getElementById('depositForm');
        console.log('[TRACE] depositForm 요소:', depositForm ? '찾음' : '없음');
        depositForm?.addEventListener('submit', (e) => {
            console.log('[TRACE] depositForm submit 이벤트 발생');
            e.preventDefault();
            this.processDeposit();
        });

        const transferForm = document.getElementById('transferForm');
        console.log('[TRACE] transferForm 요소:', transferForm ? '찾음' : '없음');
        transferForm?.addEventListener('submit', (e) => {
            console.log('[TRACE] transferForm submit 이벤트 발생');
            e.preventDefault();
            this.processTransfer();
        });

        const customerForm = document.getElementById('customerForm');
        console.log('[TRACE] customerForm 요소:', customerForm ? '찾음' : '없음');
        customerForm?.addEventListener('submit', (e) => {
            console.log('[TRACE] customerForm submit 이벤트 발생');
            e.preventDefault();
            this.createCustomer();
        });

        // Refresh buttons
        const refreshAccountsBtn = document.getElementById('refreshAccountsBtn');
        console.log('[TRACE] refreshAccountsBtn 요소:', refreshAccountsBtn ? '찾음' : '없음');
        refreshAccountsBtn?.addEventListener('click', () => {
            console.log('[TRACE] refreshAccountsBtn 클릭');
            this.loadAccounts();
        });

        const refreshCustomersBtn = document.getElementById('refreshCustomersBtn');
        console.log('[TRACE] refreshCustomersBtn 요소:', refreshCustomersBtn ? '찾음' : '없음');
        refreshCustomersBtn?.addEventListener('click', () => {
            console.log('[TRACE] refreshCustomersBtn 클릭');
            this.loadCustomers();
        });

        const refreshTransactionsBtn = document.getElementById('refreshTransactionsBtn');
        console.log('[TRACE] refreshTransactionsBtn 요소:', refreshTransactionsBtn ? '찾음' : '없음');
        refreshTransactionsBtn?.addEventListener('click', () => {
            console.log('[TRACE] refreshTransactionsBtn 클릭');
            this.loadTransactions();
        });

        // Clear logs
        const clearLogsBtn = document.getElementById('clearLogsBtn');
        console.log('[TRACE] clearLogsBtn 요소:', clearLogsBtn ? '찾음' : '없음');
        clearLogsBtn?.addEventListener('click', () => {
            console.log('[TRACE] clearLogsBtn 클릭');
            this.clearLogs();
        });

        console.log('[TRACE] setupEventListeners() 완료');
    }

    setupTabs() {
        console.log('[TRACE] setupTabs() 시작');
        const tabs = document.querySelectorAll('.tab-btn');
        const tabContents = document.querySelectorAll('.tab-content');
        console.log('[TRACE] 탭 버튼 개수:', tabs.length);
        console.log('[TRACE] 탭 컨텐츠 개수:', tabContents.length);
        
        tabs.forEach((tab, index) => {
            tab.addEventListener('click', () => {
                const targetTab = tab.getAttribute('data-tab');
                console.log(`[TRACE] 탭 클릭: ${targetTab} (인덱스: ${index})`);
                
                // Update active states with animation
                tabs.forEach(t => t.classList.remove('active'));
                tabContents.forEach(content => content.classList.remove('active'));
                
                // Activate selected tab
                setTimeout(() => {
                    tab.classList.add('active');
                    document.getElementById(targetTab + '-tab').classList.add('active');
                    console.log('[TRACE] 탭 활성화:', targetTab);
                    
                    // Load data for active tab
                    this.loadTabData(targetTab);
                }, 100);
            });
        });
        console.log('[TRACE] setupTabs() 완료');
    }

    loadTabData(tabName) {
        console.log('[TRACE] loadTabData() 호출:', tabName);
        switch(tabName) {
            case 'accounts':
                console.log('[TRACE] 계좌 탭 데이터 로딩');
                this.loadAccounts();
                break;
            case 'customers':
                console.log('[TRACE] 고객 탭 데이터 로딩');
                this.loadCustomers();
                break;
            case 'transactions':
                console.log('[TRACE] 거래내역 탭 데이터 로딩');
                this.loadTransactions();
                break;
            default:
                console.log('[TRACE] 알 수 없는 탭:', tabName);
        }
    }

    async loadInitialData() {
        console.log('[TRACE] loadInitialData() 시작');
        this.addLog('🏦 은행 시스템 초기화 중...', 'info');
        try {
            console.log('[TRACE] 초기 데이터 로딩 시작 - Promise.all');
            await Promise.all([
                this.loadAccounts(),
                this.loadCustomers(),
                this.loadTransactions()
            ]);
            console.log('[TRACE] 초기 데이터 로딩 완료');
            this.addLog('✅ 시스템 초기화 완료', 'success');
        } catch (error) {
            console.error('[TRACE] 초기 데이터 로딩 실패:', error);
            this.addLog('❌ 시스템 초기화 실패: ' + error.message, 'error');
        }
        console.log('[TRACE] loadInitialData() 완료');
    }

    formatCurrency(amount, currency = 'KRW') {
        console.log('[TRACE] formatCurrency() 호출:', amount, currency);
        const result = new Intl.NumberFormat('ko-KR', {
            style: 'currency',
            currency: currency,
            minimumFractionDigits: 0,
            maximumFractionDigits: 0
        }).format(amount);
        console.log('[TRACE] formatCurrency() 결과:', result);
        return result;
    }

    formatDate(dateString) {
        console.log('[TRACE] formatDate() 호출:', dateString);
        const date = new Date(dateString);
        const result = date.toLocaleString('ko-KR', {
            year: 'numeric',
            month: '2-digit',
            day: '2-digit',
            hour: '2-digit',
            minute: '2-digit',
            second: '2-digit'
        });
        console.log('[TRACE] formatDate() 결과:', result);
        return result;
    }

    getStatusClass(status) {
        console.log('[TRACE] getStatusClass() 호출:', status);
        const statusMap = {
            'ACTIVE': 'status-active',
            'FROZEN': 'status-frozen',
            'CLOSED': 'status-closed',
            'SUSPENDED': 'status-suspended',
            'COMPLETED': 'status-completed',
            'PENDING': 'status-pending',
            'FAILED': 'status-failed'
        };
        const result = statusMap[status] || 'status-pending';
        console.log('[TRACE] getStatusClass() 결과:', result);
        return result;
    }

    getStatusText(status) {
        console.log('[TRACE] getStatusText() 호출:', status);
        const statusMap = {
            'ACTIVE': '활성',
            'FROZEN': '동결',
            'CLOSED': '해지',
            'SUSPENDED': '일시정지',
            'COMPLETED': '완료',
            'PENDING': '처리중',
            'FAILED': '실패',
            'CANCELLED': '취소',
            'DEPOSIT': '입금',
            'WITHDRAWAL': '출금',
            'TRANSFER': '이체',
            'CHECKING': '입출금',
            'SAVINGS': '적금',
            'CREDIT': '신용',
            'BUSINESS': '사업자',
            'INDIVIDUAL': '개인',
            'LOW': '낮음',
            'MEDIUM': '보통',
            'HIGH': '높음'
        };
        const result = statusMap[status] || status;
        console.log('[TRACE] getStatusText() 결과:', result);
        return result;
    }

    addLog(message, type = 'info') {
        console.log(`[TRACE] addLog() 호출: [${type}] ${message}`);
        const logContainer = document.getElementById('logs');
        if (!logContainer) {
            console.error('[TRACE] logContainer 요소를 찾을 수 없음');
            return;
        }
        
        const logEntry = document.createElement('div');
        logEntry.className = `log-entry log-${type}`;
        
        const timestamp = new Date().toLocaleTimeString('ko-KR');
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
            timestamp: new Date().toISOString()
        });
        
        // Limit logs to prevent memory issues
        if (this.logs.length > 1000) {
            console.log('[TRACE] 로그 개수 1000개 초과, 오래된 로그 삭제');
            this.logs.shift();
        }
        console.log('[TRACE] addLog() 완료');
    }

    clearLogs() {
        console.log('[TRACE] clearLogs() 호출');
        const logContainer = document.getElementById('logs');
        if (logContainer) {
            logContainer.innerHTML = '';
        }
        this.logs = [];
        this.addLog('🗑️ 로그가 지워졌습니다', 'info');
        console.log('[TRACE] clearLogs() 완료');
    }

    showError(message) {
        console.error('[TRACE] showError() 호출:', message);
        this.addLog(`❌ ${message}`, 'error');
        alert(message);
    }

    showSuccess(message) {
        console.log('[TRACE] showSuccess() 호출:', message);
        this.addLog(`✅ ${message}`, 'success');
    }

    async makeRequest(url, options = {}) {
        console.log('[TRACE] makeRequest() 시작:', url, options.method || 'GET');
        try {
            console.log('[TRACE] fetch 요청:', url);
            const response = await fetch(url, {
                headers: {
                    'Content-Type': 'application/json',
                    ...options.headers
                },
                ...options
            });
            
            console.log('[TRACE] fetch 응답 상태:', response.status, response.statusText);
            
            if (!response.ok) {
                const errorData = await response.json();
                console.error('[TRACE] API 오류 응답:', errorData);
                throw new Error(errorData.message || `HTTP ${response.status}`);
            }
            
            const result = await response.json();
            console.log('[TRACE] API 응답 데이터:', result);
            return result;
        } catch (error) {
            console.error('[TRACE] makeRequest() 오류:', error);
            this.addLog(`🚨 API 요청 실패: ${error.message}`, 'error');
            throw error;
        }
    }

    // Account Management Methods
    async createAccount() {
        console.log('[TRACE] createAccount() 시작');
        try {
            const form = document.getElementById('accountForm');
            const formData = new FormData(form);
            const data = Object.fromEntries(formData.entries());
            
            // Convert checkbox to boolean
            data.forceFailure = formData.has('forceFailure');
            
            console.log('[TRACE] 계좌 개설 데이터:', data);
            this.addLog(`📝 계좌 개설 요청 - 고객ID: ${data.customerId}, 유형: ${data.accountType}`, 'info');
            
            console.log('[TRACE] 계좌 개설 API 호출:', `${this.API_BASE}/accounts`);
            const result = await this.makeRequest(`${this.API_BASE}/accounts`, {
                method: 'POST',
                body: JSON.stringify(data)
            });
            
            console.log('[TRACE] 계좌 개설 성공:', result);
            this.showSuccess(`계좌 개설 성공: ${result.accountNumber}`);
            form.reset();
            this.loadAccounts();
            this.populateAccountSelects();
            
        } catch (error) {
            console.error('[TRACE] createAccount() 오류:', error);
            this.showError(`계좌 개설 실패: ${error.message}`);
        }
        console.log('[TRACE] createAccount() 완료');
    }

    async loadAccounts() {
        console.log('[TRACE] loadAccounts() 시작');
        try {
            this.addLog('📋 계좌 목록 로딩 중...', 'info');
            
            console.log('[TRACE] 계좌 목록 API 호출:', `${this.API_BASE}/accounts`);
            const accounts = await this.makeRequest(`${this.API_BASE}/accounts`);
            console.log('[TRACE] 계좌 목록 수신:', accounts.length, '개');
            
            this.renderAccountsTable(accounts);
            this.populateAccountSelects();
            
            this.addLog(`✅ 계좌 목록 로딩 완료 (${accounts.length}개 계좌)`, 'success');
            
        } catch (error) {
            console.error('[TRACE] loadAccounts() 오류:', error);
            this.showError(`계좌 목록 로딩 실패: ${error.message}`);
        }
        console.log('[TRACE] loadAccounts() 완료');
    }

    renderAccountsTable(accounts) {
        console.log('[TRACE] renderAccountsTable() 시작:', accounts.length, '개 계좌');
        const tbody = document.querySelector('#accountsTable tbody');
        if (!tbody) {
            console.error('[TRACE] accountsTable tbody 요소를 찾을 수 없음');
            return;
        }
        
        tbody.innerHTML = '';
        console.log('[TRACE] 테이블 초기화 완료');

        accounts.forEach((account, index) => {
            console.log(`[TRACE] 계좌 ${index + 1} 렌더링:`, account.id, account.accountNumber);
            const row = document.createElement('tr');
            row.innerHTML = `
                <td>${account.id}</td>
                <td><strong>${account.accountNumber}</strong></td>
                <td>${account.accountHolderName || '-'}</td>
                <td><span class="status-badge ${this.getStatusClass(account.accountType)}">${this.getStatusText(account.accountType)}</span></td>
                <td><strong>${this.formatCurrency(account.balance, account.currency)}</strong></td>
                <td>${account.currency}</td>
                <td><span class="status-badge ${this.getStatusClass(account.status)}">${this.getStatusText(account.status)}</span></td>
                <td>${this.formatDate(account.createdAt)}</td>
                <td>
                    ${this.getAccountActions(account)}
                </td>
            `;
            tbody.appendChild(row);
        });
        console.log('[TRACE] renderAccountsTable() 완료');
    }

    getAccountActions(account) {
        console.log('[TRACE] getAccountActions() 호출:', account.id, account.status);
        let actions = '';
        
        if (account.status === 'ACTIVE') {
            actions += `<button class="btn-small btn-freeze" onclick="bankingSystem.freezeAccount(${account.id})">동결</button>`;
        } else if (account.status === 'FROZEN') {
            actions += `<button class="btn-small btn-activate" onclick="bankingSystem.activateAccount(${account.id})">활성화</button>`;
        }
        
        console.log('[TRACE] getAccountActions() 결과:', actions);
        return actions;
    }

    async freezeAccount(accountId) {
        console.log('[TRACE] freezeAccount() 호출:', accountId);
        if (!confirm('정말로 계좌를 동결하시겠습니까?')) {
            console.log('[TRACE] 동결 취소 (사용자 확인 거부)');
            return;
        }
        
        try {
            console.log('[TRACE] 계좌 동결 API 호출:', `${this.API_BASE}/accounts/${accountId}/freeze`);
            await this.makeRequest(`${this.API_BASE}/accounts/${accountId}/freeze`, {
                method: 'POST'
            });
            
            console.log('[TRACE] 계좌 동결 성공');
            this.showSuccess('계좌가 동결되었습니다');
            this.loadAccounts();
            
        } catch (error) {
            console.error('[TRACE] freezeAccount() 오류:', error);
            this.showError(`계좌 동결 실패: ${error.message}`);
        }
    }

    async activateAccount(accountId) {
        console.log('[TRACE] activateAccount() 호출:', accountId);
        if (!confirm('정말로 계좌를 활성화하시겠습니까?')) {
            console.log('[TRACE] 활성화 취소 (사용자 확인 거부)');
            return;
        }
        
        try {
            console.log('[TRACE] 계좌 활성화 API 호출:', `${this.API_BASE}/accounts/${accountId}/activate`);
            await this.makeRequest(`${this.API_BASE}/accounts/${accountId}/activate`, {
                method: 'POST'
            });
            
            console.log('[TRACE] 계좌 활성화 성공');
            this.showSuccess('계좌가 활성화되었습니다');
            this.loadAccounts();
            
        } catch (error) {
            console.error('[TRACE] activateAccount() 오류:', error);
            this.showError(`계좌 활성화 실패: ${error.message}`);
        }
    }

    // Transaction Methods
    async processDeposit() {
        console.log('[TRACE] processDeposit() 시작');
        try {
            const form = document.getElementById('depositForm');
            const formData = new FormData(form);
            const data = Object.fromEntries(formData.entries());
            
            console.log('[TRACE] 입금 데이터:', data);
            this.addLog(`💰 입금 처리 - 계좌ID: ${data.accountId}, 금액: ${this.formatCurrency(data.amount)}`, 'info');
            
            console.log('[TRACE] 입금 API 호출:', `${this.API_BASE}/deposit`);
            const result = await this.makeRequest(`${this.API_BASE}/deposit`, {
                method: 'POST',
                body: JSON.stringify(data)
            });
            
            console.log('[TRACE] 입금 성공:', result);
            this.showSuccess(`입금 완료: ${this.formatCurrency(data.amount)}`);
            form.reset();
            this.loadAccounts();
            this.loadTransactions();
            
        } catch (error) {
            console.error('[TRACE] processDeposit() 오류:', error);
            this.showError(`입금 실패: ${error.message}`);
        }
        console.log('[TRACE] processDeposit() 완료');
    }

    async processTransfer() {
        console.log('[TRACE] processTransfer() 시작');
        try {
            const form = document.getElementById('transferForm');
            const formData = new FormData(form);
            const data = Object.fromEntries(formData.entries());
            
            if (data.fromAccountId === data.toAccountId) {
                console.error('[TRACE] 동일 계좌 이체 시도');
                this.showError('출금계좌와 입금계좌가 동일합니다');
                return;
            }
            
            console.log('[TRACE] 이체 데이터:', data);
            this.addLog(`🔄 이체 처리 - 출금: ${data.fromAccountId}, 입금: ${data.toAccountId}, 금액: ${this.formatCurrency(data.amount)}`, 'info');
            
            console.log('[TRACE] 이체 API 호출:', `${this.API_BASE}/transfer`);
            const result = await this.makeRequest(`${this.API_BASE}/transfer`, {
                method: 'POST',
                body: JSON.stringify(data)
            });
            
            console.log('[TRACE] 이체 성공:', result);
            this.showSuccess(`이체 완료: ${this.formatCurrency(data.amount)}`);
            form.reset();
            this.loadAccounts();
            this.loadTransactions();
            
        } catch (error) {
            console.error('[TRACE] processTransfer() 오류:', error);
            this.showError(`이체 실패: ${error.message}`);
        }
        console.log('[TRACE] processTransfer() 완료');
    }

    async loadTransactions() {
        console.log('[TRACE] loadTransactions() 시작');
        try {
            this.addLog('📜 거래 내역 로딩 중...', 'info');
            
            console.log('[TRACE] 거래 내역 API 호출:', `${this.API_BASE}/transactions`);
            const transactions = await this.makeRequest(`${this.API_BASE}/transactions`);
            console.log('[TRACE] 거래 내역 수신:', transactions.length, '개');
            
            this.renderTransactionsTable(transactions);
            
            this.addLog(`✅ 거래 내역 로딩 완료 (${transactions.length}개 거래)`, 'success');
            
        } catch (error) {
            console.error('[TRACE] loadTransactions() 오류:', error);
            this.showError(`거래 내역 로딩 실패: ${error.message}`);
        }
        console.log('[TRACE] loadTransactions() 완료');
    }

    renderTransactionsTable(transactions) {
        console.log('[TRACE] renderTransactionsTable() 시작:', transactions.length, '개 거래');
        const tbody = document.querySelector('#transactionsTable tbody');
        if (!tbody) {
            console.error('[TRACE] transactionsTable tbody 요소를 찾을 수 없음');
            return;
        }
        
        tbody.innerHTML = '';
        console.log('[TRACE] 거래 테이블 초기화 완료');

        transactions.forEach((transaction, index) => {
            console.log(`[TRACE] 거래 ${index + 1} 렌더링:`, transaction.id, transaction.guid);
            const row = document.createElement('tr');
            row.innerHTML = `
                <td>${transaction.id}</td>
                <td><strong>${transaction.guid}</strong></td>
                <td>${transaction.fromAccountId || '-'}</td>
                <td>${transaction.toAccountId || '-'}</td>
                <td><span class="status-badge ${this.getStatusClass(transaction.transactionType)}">${this.getStatusText(transaction.transactionType)}</span></td>
                <td><strong>${this.formatCurrency(transaction.amount, transaction.currency)}</strong></td>
                <td>${transaction.currency}</td>
                <td><span class="status-badge ${this.getStatusClass(transaction.status)}">${this.getStatusText(transaction.status)}</span></td>
                <td>${transaction.referenceNumber || '-'}</td>
                <td>${transaction.description || '-'}</td>
                <td>${this.formatDate(transaction.createdAt)}</td>
            `;
            tbody.appendChild(row);
        });
        console.log('[TRACE] renderTransactionsTable() 완료');
    }

    // Customer Methods
    async createCustomer() {
        console.log('[TRACE] createCustomer() 시작');
        try {
            const form = document.getElementById('customerForm');
            const formData = new FormData(form);
            const data = Object.fromEntries(formData.entries());
            
            console.log('[TRACE] 고객 등록 데이터:', data);
            this.addLog(`👤 고객 등록 요청 - ${data.name} (${data.customerType})`, 'info');
            
            console.log('[TRACE] 고객 등록 API 호출:', `${this.API_BASE}/customers`);
            const result = await this.makeRequest(`${this.API_BASE}/customers`, {
                method: 'POST',
                body: JSON.stringify(data)
            });
            
            console.log('[TRACE] 고객 등록 성공:', result);
            this.showSuccess(`고객 등록 완료: ${result.customerNumber}`);
            form.reset();
            this.loadCustomers();
            
        } catch (error) {
            console.error('[TRACE] createCustomer() 오류:', error);
            this.showError(`고객 등록 실패: ${error.message}`);
        }
        console.log('[TRACE] createCustomer() 완료');
    }

    async loadCustomers() {
        console.log('[TRACE] loadCustomers() 시작');
        try {
            this.addLog('👥 고객 목록 로딩 중...', 'info');
            
            console.log('[TRACE] 고객 목록 API 호출:', `${this.API_BASE}/customers`);
            const customers = await this.makeRequest(`${this.API_BASE}/customers`);
            console.log('[TRACE] 고객 목록 수신:', customers.length, '명');
            
            this.renderCustomersTable(customers);
            
            this.addLog(`✅ 고객 목록 로딩 완료 (${customers.length}명)`, 'success');
            
        } catch (error) {
            console.error('[TRACE] loadCustomers() 오류:', error);
            this.showError(`고객 목록 로딩 실패: ${error.message}`);
        }
        console.log('[TRACE] loadCustomers() 완료');
    }

    renderCustomersTable(customers) {
        console.log('[TRACE] renderCustomersTable() 시작:', customers.length, '명 고객');
        const tbody = document.querySelector('#customersTable tbody');
        if (!tbody) {
            console.error('[TRACE] customersTable tbody 요소를 찾을 수 없음');
            return;
        }
        
        tbody.innerHTML = '';
        console.log('[TRACE] 고객 테이블 초기화 완료');

        customers.forEach((customer, index) => {
            console.log(`[TRACE] 고객 ${index + 1} 렌더링:`, customer.id, customer.customerNumber);
            const row = document.createElement('tr');
            row.innerHTML = `
                <td>${customer.id}</td>
                <td><strong>${customer.customerNumber}</strong></td>
                <td>${customer.name}</td>
                <td>${customer.phoneNumber}</td>
                <td>${customer.email}</td>
                <td><span class="status-badge ${this.getStatusClass(customer.customerType)}">${this.getStatusText(customer.customerType)}</span></td>
                <td><span class="status-badge">${this.getStatusText(customer.riskLevel)}</span></td>
                <td><span class="status-badge ${this.getStatusClass(customer.status)}">${this.getStatusText(customer.status)}</span></td>
                <td>${this.formatDate(customer.createdAt)}</td>
                <td>
                    <button class="btn-small btn-cancel" onclick="bankingSystem.suspendCustomer(${customer.id})">정지</button>
                </td>
            `;
            tbody.appendChild(row);
        });
        console.log('[TRACE] renderCustomersTable() 완료');
    }

    async suspendCustomer(customerId) {
        console.log('[TRACE] suspendCustomer() 호출:', customerId);
        if (!confirm('정말로 고객을 정지하시겠습니까?')) {
            console.log('[TRACE] 고객 정지 취소 (사용자 확인 거부)');
            return;
        }
        
        try {
            console.log('[TRACE] 고객 정지 API 호출:', `${this.API_BASE}/customers/${customerId}/suspend`);
            await this.makeRequest(`${this.API_BASE}/customers/${customerId}/suspend`, {
                method: 'POST'
            });
            
            console.log('[TRACE] 고객 정지 성공');
            this.showSuccess('고객이 정지되었습니다');
            this.loadCustomers();
            
        } catch (error) {
            console.error('[TRACE] suspendCustomer() 오류:', error);
            this.showError(`고객 정지 실패: ${error.message}`);
        }
    }

    // Utility Methods
    populateAccountSelects() {
        console.log('[TRACE] populateAccountSelects() 시작');
        const selects = ['depositAccountId', 'withdrawAccountId', 'fromAccountId', 'toAccountId'];
        
        selects.forEach(selectId => {
            console.log(`[TRACE] select 처리: ${selectId}`);
            const select = document.getElementById(selectId);
            if (!select) {
                console.log(`[TRACE] ${selectId} 요소 없음, 건너뜀`);
                return;
            }
            
            // Get current accounts
            const tbody = document.querySelector('#accountsTable tbody');
            if (!tbody) {
                console.error('[TRACE] accountsTable tbody 요소를 찾을 수 없음');
                return;
            }
            
            const accounts = Array.from(tbody.querySelectorAll('tr')).map(row => {
                const cells = row.querySelectorAll('td');
                return {
                    id: cells[0].textContent,
                    accountNumber: cells[1].textContent.trim(),
                    status: cells[6].textContent.trim()
                };
            });
            
            console.log('[TRACE] 계좌 목록에서 선택 옵션 생성:', accounts.length, '개');
            
            // Clear existing options except the first one
            const firstOption = select.querySelector('option');
            select.innerHTML = '';
            if (firstOption) {
                select.appendChild(firstOption);
            }
            
            // Add account options
            let activeCount = 0;
            accounts.forEach(account => {
                if (account.status.includes('활성')) {
                    const option = document.createElement('option');
                    option.value = account.id;
                    option.textContent = `${account.accountNumber}`;
                    select.appendChild(option);
                    activeCount++;
                }
            });
            console.log(`[TRACE] ${selectId}에 활성 계좌 ${activeCount}개 추가`);
        });
        console.log('[TRACE] populateAccountSelects() 완료');
    }
}

// Initialize banking system when DOM is loaded
console.log('[TRACE] DOMContentLoaded 리스너 등록');
document.addEventListener('DOMContentLoaded', function() {
    console.log('[TRACE] DOMContentLoaded 콜백 실행 - BankingSystem 인스턴스 생성 시작');
    try {
        window.bankingSystem = new BankingSystem();
        console.log('[TRACE] BankingSystem 인스턴스 생성 완료, window.bankingSystem:', window.bankingSystem);
    } catch (error) {
        console.error('[TRACE] BankingSystem 인스턴스 생성 오류:', error);
    }
});

console.log('[TRACE] script.js 로딩 완료');
