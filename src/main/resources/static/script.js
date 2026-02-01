// Banking System JavaScript
class BankingSystem {
    constructor() {
        this.API_BASE = '/api/banking';
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
        document.getElementById('accountForm')?.addEventListener('submit', (e) => {
            e.preventDefault();
            this.createAccount();
        });

        document.getElementById('depositForm')?.addEventListener('submit', (e) => {
            e.preventDefault();
            this.processDeposit();
        });

        document.getElementById('transferForm')?.addEventListener('submit', (e) => {
            e.preventDefault();
            this.processTransfer();
        });

        document.getElementById('customerForm')?.addEventListener('submit', (e) => {
            e.preventDefault();
            this.createCustomer();
        });

        // Refresh buttons
        document.getElementById('refreshAccountsBtn')?.addEventListener('click', () => this.loadAccounts());
        document.getElementById('refreshCustomersBtn')?.addEventListener('click', () => this.loadCustomers());
        document.getElementById('refreshTransactionsBtn')?.addEventListener('click', () => this.loadTransactions());

        // Clear logs
        document.getElementById('clearLogsBtn')?.addEventListener('click', () => this.clearLogs());
    }

    setupTabs() {
        const tabs = document.querySelectorAll('.tab-btn');
        const tabContents = document.querySelectorAll('.tab-content');
        
        tabs.forEach(tab => {
            tab.addEventListener('click', () => {
                const targetTab = tab.getAttribute('data-tab');
                
                // Update active states with animation
                tabs.forEach(t => t.classList.remove('active'));
                tabContents.forEach(content => content.classList.remove('active'));
                
                // Activate selected tab
                setTimeout(() => {
                    tab.classList.add('active');
                    document.getElementById(targetTab + '-tab').classList.add('active');
                    
                    // Load data for active tab
                    this.loadTabData(targetTab);
                }, 100);
            });
        });
    }

    loadTabData(tabName) {
        switch(tabName) {
            case 'accounts':
                this.loadAccounts();
                break;
            case 'customers':
                this.loadCustomers();
                break;
            case 'transactions':
                this.loadTransactions();
                break;
        }
    }

    async loadInitialData() {
        this.addLog('🏦 은행 시스템 초기화 중...', 'info');
        await Promise.all([
            this.loadAccounts(),
            this.loadCustomers(),
            this.loadTransactions()
        ]);
        this.addLog('✅ 시스템 초기화 완료', 'success');
    }

    formatCurrency(amount, currency = 'KRW') {
        return new Intl.NumberFormat('ko-KR', {
            style: 'currency',
            currency: currency,
            minimumFractionDigits: 0,
            maximumFractionDigits: 0
        }).format(amount);
    }

    formatDate(dateString) {
        const date = new Date(dateString);
        return date.toLocaleString('ko-KR', {
            year: 'numeric',
            month: '2-digit',
            day: '2-digit',
            hour: '2-digit',
            minute: '2-digit',
            second: '2-digit'
        });
    }

    getStatusClass(status) {
        const statusMap = {
            'ACTIVE': 'status-active',
            'FROZEN': 'status-frozen',
            'CLOSED': 'status-closed',
            'SUSPENDED': 'status-suspended',
            'COMPLETED': 'status-completed',
            'PENDING': 'status-pending',
            'FAILED': 'status-failed'
        };
        return statusMap[status] || 'status-pending';
    }

    getStatusText(status) {
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
        return statusMap[status] || status;
    }

    addLog(message, type = 'info') {
        const logContainer = document.getElementById('logs');
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
            this.logs.shift();
        }
    }

    clearLogs() {
        const logContainer = document.getElementById('logs');
        logContainer.innerHTML = '';
        this.logs = [];
        this.addLog('🗑️ 로그가 지워졌습니다', 'info');
    }

    showError(message) {
        this.addLog(`❌ ${message}`, 'error');
        alert(message);
    }

    showSuccess(message) {
        this.addLog(`✅ ${message}`, 'success');
    }

    async makeRequest(url, options = {}) {
        try {
            const response = await fetch(url, {
                headers: {
                    'Content-Type': 'application/json',
                    ...options.headers
                },
                ...options
            });
            
            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.message || `HTTP ${response.status}`);
            }
            
            return await response.json();
        } catch (error) {
            this.addLog(`🚨 API 요청 실패: ${error.message}`, 'error');
            throw error;
        }
    }

    // Account Management Methods
    async createAccount() {
        try {
            const formData = new FormData(document.getElementById('accountForm'));
            const data = Object.fromEntries(formData.entries());
            
            // Convert checkbox to boolean
            data.forceFailure = formData.has('forceFailure');
            
            this.addLog(`📝 계좌 개설 요청 - 고객ID: ${data.customerId}, 유형: ${data.accountType}`, 'info');
            
            const result = await this.makeRequest(`${this.API_BASE}/accounts`, {
                method: 'POST',
                body: JSON.stringify(data)
            });
            
            this.showSuccess(`계좌 개설 성공: ${result.accountNumber}`);
            document.getElementById('accountForm').reset();
            this.loadAccounts();
            this.populateAccountSelects();
            
        } catch (error) {
            this.showError(`계좌 개설 실패: ${error.message}`);
        }
    }

    async loadAccounts() {
        try {
            this.addLog('📋 계좌 목록 로딩 중...', 'info');
            
            const accounts = await this.makeRequest(`${this.API_BASE}/accounts`);
            this.renderAccountsTable(accounts);
            this.populateAccountSelects();
            
            this.addLog(`✅ 계좌 목록 로딩 완료 (${accounts.length}개 계좌)`, 'success');
            
        } catch (error) {
            this.showError(`계좌 목록 로딩 실패: ${error.message}`);
        }
    }

    renderAccountsTable(accounts) {
        const tbody = document.querySelector('#accountsTable tbody');
        tbody.innerHTML = '';

        accounts.forEach(account => {
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
    }

    getAccountActions(account) {
        let actions = '';
        
        if (account.status === 'ACTIVE') {
            actions += `<button class="btn-small btn-freeze" onclick="bankingSystem.freezeAccount(${account.id})">동결</button>`;
        } else if (account.status === 'FROZEN') {
            actions += `<button class="btn-small btn-activate" onclick="bankingSystem.activateAccount(${account.id})">활성화</button>`;
        }
        
        return actions;
    }

    async freezeAccount(accountId) {
        if (!confirm('정말로 계좌를 동결하시겠습니까?')) return;
        
        try {
            await this.makeRequest(`${this.API_BASE}/accounts/${accountId}/freeze`, {
                method: 'POST'
            });
            
            this.showSuccess('계좌가 동결되었습니다');
            this.loadAccounts();
            
        } catch (error) {
            this.showError(`계좌 동결 실패: ${error.message}`);
        }
    }

    async activateAccount(accountId) {
        if (!confirm('정말로 계좌를 활성화하시겠습니까?')) return;
        
        try {
            await this.makeRequest(`${this.API_BASE}/accounts/${accountId}/activate`, {
                method: 'POST'
            });
            
            this.showSuccess('계좌가 활성화되었습니다');
            this.loadAccounts();
            
        } catch (error) {
            this.showError(`계좌 활성화 실패: ${error.message}`);
        }
    }

    // Transaction Methods
    async processDeposit() {
        try {
            const formData = new FormData(document.getElementById('depositForm'));
            const data = Object.fromEntries(formData.entries());
            
            this.addLog(`💰 입금 처리 - 계좌ID: ${data.accountId}, 금액: ${this.formatCurrency(data.amount)}`, 'info');
            
            const result = await this.makeRequest(`${this.API_BASE}/deposit`, {
                method: 'POST',
                body: JSON.stringify(data)
            });
            
            this.showSuccess(`입금 완료: ${this.formatCurrency(data.amount)}`);
            document.getElementById('depositForm').reset();
            this.loadAccounts();
            this.loadTransactions();
            
        } catch (error) {
            this.showError(`입금 실패: ${error.message}`);
        }
    }

    async processTransfer() {
        try {
            const formData = new FormData(document.getElementById('transferForm'));
            const data = Object.fromEntries(formData.entries());
            
            if (data.fromAccountId === data.toAccountId) {
                this.showError('출금계좌와 입금계좌가 동일합니다');
                return;
            }
            
            this.addLog(`🔄 이체 처리 - 출금: ${data.fromAccountId}, 입금: ${data.toAccountId}, 금액: ${this.formatCurrency(data.amount)}`, 'info');
            
            const result = await this.makeRequest(`${this.API_BASE}/transfer`, {
                method: 'POST',
                body: JSON.stringify(data)
            });
            
            this.showSuccess(`이체 완료: ${this.formatCurrency(data.amount)}`);
            document.getElementById('transferForm').reset();
            this.loadAccounts();
            this.loadTransactions();
            
        } catch (error) {
            this.showError(`이체 실패: ${error.message}`);
        }
    }

    async loadTransactions() {
        try {
            this.addLog('📜 거래 내역 로딩 중...', 'info');
            
            const transactions = await this.makeRequest(`${this.API_BASE}/transactions`);
            this.renderTransactionsTable(transactions);
            
            this.addLog(`✅ 거래 내역 로딩 완료 (${transactions.length}개 거래)`, 'success');
            
        } catch (error) {
            this.showError(`거래 내역 로딩 실패: ${error.message}`);
        }
    }

    renderTransactionsTable(transactions) {
        const tbody = document.querySelector('#transactionsTable tbody');
        tbody.innerHTML = '';

        transactions.forEach(transaction => {
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
    }

    // Customer Methods
    async createCustomer() {
        try {
            const formData = new FormData(document.getElementById('customerForm'));
            const data = Object.fromEntries(formData.entries());
            
            this.addLog(`👤 고객 등록 요청 - ${data.name} (${data.customerType})`, 'info');
            
            const result = await this.makeRequest(`${this.API_BASE}/customers`, {
                method: 'POST',
                body: JSON.stringify(data)
            });
            
            this.showSuccess(`고객 등록 완료: ${result.customerNumber}`);
            document.getElementById('customerForm').reset();
            this.loadCustomers();
            
        } catch (error) {
            this.showError(`고객 등록 실패: ${error.message}`);
        }
    }

    async loadCustomers() {
        try {
            this.addLog('👥 고객 목록 로딩 중...', 'info');
            
            const customers = await this.makeRequest(`${this.API_BASE}/customers`);
            this.renderCustomersTable(customers);
            
            this.addLog(`✅ 고객 목록 로딩 완료 (${customers.length}명)`, 'success');
            
        } catch (error) {
            this.showError(`고객 목록 로딩 실패: ${error.message}`);
        }
    }

    renderCustomersTable(customers) {
        const tbody = document.querySelector('#customersTable tbody');
        tbody.innerHTML = '';

        customers.forEach(customer => {
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
    }

    async suspendCustomer(customerId) {
        if (!confirm('정말로 고객을 정지하시겠습니까?')) return;
        
        try {
            await this.makeRequest(`${this.API_BASE}/customers/${customerId}/suspend`, {
                method: 'POST'
            });
            
            this.showSuccess('고객이 정지되었습니다');
            this.loadCustomers();
            
        } catch (error) {
            this.showError(`고객 정지 실패: ${error.message}`);
        }
    }

    // Utility Methods
    populateAccountSelects() {
        const selects = ['depositAccountId', 'withdrawAccountId', 'fromAccountId', 'toAccountId'];
        
        selects.forEach(selectId => {
            const select = document.getElementById(selectId);
            if (!select) return;
            
            // Get current accounts
            const tbody = document.querySelector('#accountsTable tbody');
            const accounts = Array.from(tbody.querySelectorAll('tr')).map(row => {
                const cells = row.querySelectorAll('td');
                return {
                    id: cells[0].textContent,
                    accountNumber: cells[1].textContent.trim(),
                    status: cells[6].textContent.trim()
                };
            });
            
            // Clear existing options except the first one
            const firstOption = select.querySelector('option');
            select.innerHTML = '';
            select.appendChild(firstOption);
            
            // Add account options
            accounts.forEach(account => {
                if (account.status.includes('활성')) {
                    const option = document.createElement('option');
                    option.value = account.id;
                    option.textContent = `${account.accountNumber}`;
                    select.appendChild(option);
                }
            });
        });
    }
}

// Initialize banking system when DOM is loaded
document.addEventListener('DOMContentLoaded', function() {
    window.bankingSystem = new BankingSystem();
});
