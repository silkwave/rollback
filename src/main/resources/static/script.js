class RollbackDemo {
    constructor() {
        this.setupEventListeners();
        this.loadOrders();
    }

    setupEventListeners() {
        // 주문 폼 제출
        document.getElementById('orderForm').addEventListener('submit', (e) => {
            e.preventDefault();
            this.createOrder();
        });

        // 새로고침 버튼
        document.getElementById('refreshBtn').addEventListener('click', () => {
            this.loadOrders();
        });

        // 로그 지우기 버튼
        document.getElementById('clearLogsBtn').addEventListener('click', () => {
            this.clearLogs();
        });

        // // 주기적으로 주문 목록 새로고침 (5초마다)
        // setInterval(() => {
        //     this.loadOrders();
        // }, 5000);
    }

    async createOrder() {
        const formData = new FormData(document.getElementById('orderForm'));
        const orderData = {
            customerName: formData.get('customerName'),
            amount: parseInt(formData.get('amount')),
            forcePaymentFailure: formData.get('forcePaymentFailure') === 'on'
        };

        this.addLog(`📝 주문 생성 요청: ${orderData.customerName} - ${orderData.amount}원`, 'info');
        if (orderData.forcePaymentFailure) {
            this.addLog('⚠️ 결제 실패 강제 발생 옵션이 활성화되었습니다', 'warning');
        }

        try {
            const response = await fetch('/api/orders', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(orderData)
            });

            const result = await response.json();

            if (response.ok && result.success) {
                this.addLog(`✅ 주문 성공: 주문 ID ${result.order.id}`, 'success');
                this.addLog(`💳 결제 완료`, 'success');
                document.getElementById('orderForm').reset();
            } else {
                this.addLog(`❌ 주문 실패: ${result.message}`, 'error');
                this.addLog(`🔄 트랜잭션이 롤백됩니다`, 'warning');
            }
        } catch (error) {
            this.addLog(`🚨 네트워크 오류: ${error.message}`, 'error');
        }

        // 주문 목록 새로고침
        // setTimeout(() => {
        //     this.loadOrders();
        // }, 1000);
    }

    async loadOrders() {
        try {
            const response = await fetch('/api/orders');
            const orders = await response.json();
            this.displayOrders(orders);
        } catch (error) {
            this.addLog(`주문 목록 로딩 실패: ${error.message}`, 'error');
        }
    }

    displayOrders(orders) {
        const tbody = document.querySelector('#ordersTable tbody');
        tbody.innerHTML = '';

        if (orders.length === 0) {
            tbody.innerHTML = '<tr><td colspan="5" style="text-align: center; color: #7f8c8d;">주문이 없습니다</td></tr>';
            return;
        }

        orders.forEach(order => {
            const row = document.createElement('tr');
            row.innerHTML = `
                <td>${order.id}</td>
                <td>${order.customerName}</td>
                <td>${order.amount.toLocaleString()}원</td>
                <td><span class="status status-${order.status.toLowerCase()}">${this.getStatusText(order.status)}</span></td>
                <td>${new Date().toLocaleString()}</td>
            `;
            tbody.appendChild(row);
        });

        this.addLog(`📋 주문 목록이 업데이트되었습니다 (${orders.length}개 주문)`, 'info');
    }

    getStatusText(status) {
        const statusMap = {
            'CREATED': '생성됨',
            'PAID': '결제완료'
        };
        return statusMap[status] || status;
    }

    addLog(message, type = 'info') {
        const logsContainer = document.getElementById('logs');
        const timestamp = new Date().toLocaleTimeString();
        const logEntry = document.createElement('div');
        logEntry.className = `log-entry log-${type}`;
        logEntry.textContent = `[${timestamp}] ${message}`;
        
        logsContainer.appendChild(logEntry);
        logsContainer.scrollTop = logsContainer.scrollHeight;

        // 로그 개수 제한 (최대 100개)
        while (logsContainer.children.length > 100) {
            logsContainer.removeChild(logsContainer.firstChild);
        }
    }

    clearLogs() {
        document.getElementById('logs').innerHTML = '';
        this.addLog('🗑️ 로그가 지워졌습니다', 'info');
    }
}

// 페이지 로드가 완료되면 애플리케이션 초기화
document.addEventListener('DOMContentLoaded', () => {
    new RollbackDemo();
});