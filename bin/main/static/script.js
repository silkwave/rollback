class RollbackDemo {
    constructor() {
        this.setupEventListeners();
        this.setupTabs();
        this.loadOrders();
    }

    setupTabs() {
        // 탭 버튼 클릭 이벤트
        document.querySelectorAll('.tab-btn').forEach(btn => {
            btn.addEventListener('click', (e) => {
                const tabName = e.target.dataset.tab;
                this.switchTab(tabName);
            });
        });
    }

    switchTab(tabName) {
        // 탭 버튼 상태 업데이트
        document.querySelectorAll('.tab-btn').forEach(btn => {
            btn.classList.remove('active');
        });
        document.querySelector(`[data-tab="${tabName}"]`).classList.add('active');

        // 탭 콘텐츠 표시/숨김
        document.querySelectorAll('.tab-content').forEach(content => {
            content.classList.remove('active');
        });
        document.getElementById(`${tabName}-tab`).classList.add('active');

        // 해당 탭 데이터 로드
        switch(tabName) {
            case 'orders':
                this.loadOrders();
                break;
            case 'inventory':
                this.loadInventory();
                this.loadLowStock();
                break;
            case 'shipping':
                this.loadShipments();
                break;
        }
    }

    setupEventListeners() {
        // 주문 폼 제출
        if (document.getElementById('orderForm')) {
            document.getElementById('orderForm').addEventListener('submit', (e) => {
                e.preventDefault();
                this.createOrder();
            });
        }

        // 재고 폼 제출
        if (document.getElementById('inventoryForm')) {
            document.getElementById('inventoryForm').addEventListener('submit', (e) => {
                e.preventDefault();
                this.createInventory();
            });
        }

        // 배송 폼 제출
        if (document.getElementById('shipmentForm')) {
            document.getElementById('shipmentForm').addEventListener('submit', (e) => {
                e.preventDefault();
                this.createShipment();
            });
        }

        // 새로고침 버튼들
        const refreshBtn = document.getElementById('refreshBtn');
        if (refreshBtn) {
            refreshBtn.addEventListener('click', () => this.loadOrders());
        }

        const refreshInventoryBtn = document.getElementById('refreshInventoryBtn');
        if (refreshInventoryBtn) {
            refreshInventoryBtn.addEventListener('click', () => {
                this.loadInventory();
                this.loadLowStock();
            });
        }

        const refreshLowStockBtn = document.getElementById('refreshLowStockBtn');
        if (refreshLowStockBtn) {
            refreshLowStockBtn.addEventListener('click', () => this.loadLowStock());
        }

        const refreshShipmentBtn = document.getElementById('refreshShipmentBtn');
        if (refreshShipmentBtn) {
            refreshShipmentBtn.addEventListener('click', () => this.loadShipments());
        }

        // 로그 지우기 버튼
        const clearLogsBtn = document.getElementById('clearLogsBtn');
        if (clearLogsBtn) {
            clearLogsBtn.addEventListener('click', () => this.clearLogs());
        }
    }

    async createOrder() {
        // 상품 선택 유효성 검사
        const productNameSelect = document.getElementById('productName');
        if (productNameSelect.selectedIndex === 0) {
            this.addLog('⚠️ 상품을 선택해주세요', 'warning');
            productNameSelect.focus();
            return;
        }

        const formData = new FormData(document.getElementById('orderForm'));
        const orderData = {
            customerName: formData.get('customerName'),
            productName: formData.get('productName'),
            quantity: parseInt(formData.get('quantity')),
            amount: parseInt(formData.get('amount')),
            forcePaymentFailure: formData.get('forcePaymentFailure') === 'on'
        };

        this.addLog(`📝 주문 생성 요청: ${orderData.customerName} - ${orderData.productName} ${orderData.quantity}개 (${orderData.amount}원)`, 'info');
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
                this.addLog(`✅ 주문 생성 성공: ${result.order.id}번 주문`, 'success');
                document.getElementById('orderForm').reset();
                this.loadOrders();
            } else {
                this.addLog(`❌ 주문 생성 실패: ${result.message}`, 'error');
            }
        } catch (error) {
            this.addLog(`🚨 주문 생성 오류: ${error.message}`, 'error');
        }
    }
    

    async createInventory() {
        const formData = new FormData(document.getElementById('inventoryForm'));
        const inventoryData = {
            productName: formData.get('productName'),
            currentStock: parseInt(formData.get('currentStock')),
            minStockLevel: parseInt(formData.get('minStockLevel')) || 10
        };

        this.addLog(`📦 재고 등록 요청: ${inventoryData.productName} - ${inventoryData.currentStock}개`, 'info');

        try {
            const response = await fetch('/api/orders/inventory', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(inventoryData)
            });

            const result = await response.json();

            if (result.success) {
                this.addLog(`✅ 재고 등록 성공: ${result.inventory.id}번 재고`, 'success');
                document.getElementById('inventoryForm').reset();
                this.loadInventory();
            } else {
                this.addLog(`❌ 재고 등록 실패: ${result.message}`, 'error');
            }
        } catch (error) {
            this.addLog(`🚨 재고 등록 오류: ${error.message}`, 'error');
        }
    }

    async createShipment() {
        const formData = new FormData(document.getElementById('shipmentForm'));
        const shipmentData = {
            orderId: parseInt(formData.get('orderId')),
            shippingAddress: formData.get('shippingAddress')
        };

        this.addLog(`🚚 배송 생성 요청: 주문 ${shipmentData.orderId}번`, 'info');

        try {
            const response = await fetch(`/api/orders/${shipmentData.orderId}/shipment`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(shipmentData)
            });

            const result = await response.json();

            if (result.success) {
                this.addLog(`✅ 배송 생성 성공: ${result.shipment.id}번 배송`, 'success');
                document.getElementById('shipmentForm').reset();
                this.loadShipments();
            } else {
                this.addLog(`❌ 배송 생성 실패: ${result.message}`, 'error');
            }
        } catch (error) {
            this.addLog(`🚨 배송 생성 오류: ${error.message}`, 'error');
        }
    }

    async loadOrders() {
        try {
            const response = await fetch('/api/orders');
            const orders = await response.json();

            const tbody = document.querySelector('#ordersTable tbody');
            tbody.innerHTML = '';

            orders.forEach(order => {
                const row = document.createElement('tr');
                row.innerHTML = `
                    <td>${order.id}</td>
                    <td>${order.customerName}</td>
                    <td>${order.productName || '-'}</td>
                    <td>${order.quantity || '-'}</td>
                    <td>${order.amount?.toLocaleString() || '-'}원</td>
                    <td><span class="status-badge status-${order.status?.toLowerCase()}">${this.getStatusText(order.status)}</span></td>
                    <td>${new Date(order.createdAt).toLocaleString()}</td>
                    <td>
                        ${this.getOrderActions(order)}
                    </td>
                `;
                tbody.appendChild(row);
            });
        } catch (error) {
            this.addLog(`🚨 주문 목록 로딩 오류: ${error.message}`, 'error');
        }
    }

    async loadInventory() {
        try {
            const response = await fetch('/api/orders/inventory');
            const inventory = await response.json();

            const tbody = document.querySelector('#inventoryTable tbody');
            tbody.innerHTML = '';

            inventory.forEach(item => {
                const availableStock = item.currentStock - item.reservedStock;
                const isLowStock = availableStock <= item.minStockLevel;
                
                const row = document.createElement('tr');
                row.innerHTML = `
                    <td>${item.id}</td>
                    <td>${item.productName}</td>
                    <td>${item.currentStock}</td>
                    <td>${item.reservedStock}</td>
                    <td><span class="${isLowStock ? 'low-stock' : 'good-stock'}">${availableStock}</span></td>
                    <td>${item.minStockLevel}</td>
                    <td><span class="status-badge status-${isLowStock ? 'low' : 'good'}">${isLowStock ? '재고 부족' : '정상'}</span></td>
                `;
                tbody.appendChild(row);
            });
        } catch (error) {
            this.addLog(`🚨 재고 목록 로딩 오류: ${error.message}`, 'error');
        }
    }

    async loadLowStock() {
        try {
            const response = await fetch('/api/orders/inventory/low-stock');
            const lowStockItems = await response.json();

            const tbody = document.querySelector('#lowStockTable tbody');
            tbody.innerHTML = '';

            if (lowStockItems.length === 0) {
                tbody.innerHTML = '<tr><td colspan="6" class="text-center">재고 부족 상품이 없습니다</td></tr>';
                return;
            }

            lowStockItems.forEach(item => {
                const availableStock = item.currentStock - item.reservedStock;
                const shortage = Math.max(0, item.minStockLevel - availableStock);
                
                const row = document.createElement('tr');
                row.className = 'low-stock-row';
                row.innerHTML = `
                    <td>${item.productName}</td>
                    <td>${item.currentStock}</td>
                    <td>${item.reservedStock}</td>
                    <td class="low-stock">${availableStock}</td>
                    <td>${item.minStockLevel}</td>
                    <td class="shortage">${shortage}</td>
                `;
                tbody.appendChild(row);
            });
        } catch (error) {
            this.addLog(`🚨 재고 부족 목록 로딩 오류: ${error.message}`, 'error');
        }
    }

    async loadShipments() {
        try {
            const response = await fetch('/api/orders');
            const orders = await response.json();
            
            // 배송 정보가 있는 주문만 필터링
            const ordersWithShipments = await Promise.all(
                orders.map(async (order) => {
                    try {
                        const shipmentResponse = await fetch(`/api/orders/${order.id}/shipment`);
                        if (shipmentResponse.ok) {
                            const shipment = await shipmentResponse.json();
                            return { ...order, shipment };
                        }
                        return null;
                    } catch {
                        return null;
                    }
                })
            );

            const shipments = ordersWithShipments.filter(Boolean);

            const tbody = document.querySelector('#shipmentTable tbody');
            tbody.innerHTML = '';

            shipments.forEach(item => {
                const shipment = item.shipment;
                const row = document.createElement('tr');
                row.innerHTML = `
                    <td>${shipment.id}</td>
                    <td>${item.id}</td>
                    <td>${shipment.trackingNumber || '-'}</td>
                    <td>${shipment.carrier || '-'}</td>
                    <td><span class="status-badge status-${shipment.status?.toLowerCase()}">${this.getShipmentStatusText(shipment.status)}</span></td>
                    <td>${shipment.shippingAddress || '-'}</td>
                    <td>${shipment.estimatedDelivery ? new Date(shipment.estimatedDelivery).toLocaleDateString() : '-'}</td>
                    <td>${this.getShipmentActions(shipment)}</td>
                `;
                tbody.appendChild(row);
            });
        } catch (error) {
            this.addLog(`🚨 배송 목록 로딩 오류: ${error.message}`, 'error');
        }
    }

    async updateOrder(orderId, orderData) {
        try {
            const response = await fetch(`/api/orders/${orderId}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(orderData)
            });

            const result = await response.json();

            if (result.success) {
                this.addLog(`✅ 주문 수정 성공: ${orderId}번 주문`, 'success');
                this.loadOrders();
            } else {
                this.addLog(`❌ 주문 수정 실패: ${result.message}`, 'error');
            }
        } catch (error) {
            this.addLog(`🚨 주문 수정 오류: ${error.message}`, 'error');
        }
    }

    async cancelOrder(orderId) {
        if (!confirm(`정말로 ${orderId}번 주문을 취소하시겠습니까?`)) {
            return;
        }

        try {
            const response = await fetch(`/api/orders/${orderId}/cancel`, {
                method: 'POST'
            });

            const result = await response.json();

            if (result.success) {
                this.addLog(`✅ 주문 취소 성공: ${orderId}번 주문`, 'success');
                this.loadOrders();
            } else {
                this.addLog(`❌ 주문 취소 실패: ${result.message}`, 'error');
            }
        } catch (error) {
            this.addLog(`🚨 주문 취소 오류: ${error.message}`, 'error');
        }
    }

    async shipOrder(shipmentId, carrier = 'CJ대한통운') {
        try {
            const response = await fetch(`/api/orders/shipment/${shipmentId}/ship`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ carrier })
            });

            const result = await response.json();

            if (result.success) {
                this.addLog(`✅ 배송 시작 성공: ${shipmentId}번 배송 (${result.shipment.trackingNumber})`, 'success');
                this.loadShipments();
            } else {
                this.addLog(`❌ 배송 시작 실패: ${result.message}`, 'error');
            }
        } catch (error) {
            this.addLog(`🚨 배송 시작 오류: ${error.message}`, 'error');
        }
    }

    async deliverOrder(shipmentId) {
        if (!confirm(`정말로 ${shipmentId}번 배송을 완료 처리하시겠습니까?`)) {
            return;
        }

        try {
            const response = await fetch(`/api/orders/shipment/${shipmentId}/deliver`, {
                method: 'POST'
            });

            const result = await response.json();

            if (result.success) {
                this.addLog(`✅ 배송 완료 성공: ${shipmentId}번 배송`, 'success');
                this.loadShipments();
            } else {
                this.addLog(`❌ 배송 완료 실패: ${result.message}`, 'error');
            }
        } catch (error) {
            this.addLog(`🚨 배송 완료 오류: ${error.message}`, 'error');
        }
    }

    getStatusText(status) {
        const statusMap = {
            'CREATED': '주문 생성',
            'PAID': '결제 완료',
            'PREPARING': '배송 준비중',
            'SHIPPED': '배송 중',
            'DELIVERED': '배송 완료',
            'CANCELLED': '주문 취소',
            'FAILED': '처리 실패'
        };
        return statusMap[status] || status;
    }

    getShipmentStatusText(status) {
        const statusMap = {
            'PREPARING': '배송 준비중',
            'SHIPPED': '배송 중',
            'IN_TRANSIT': '배송 중',
            'DELIVERED': '배송 완료',
            'CANCELLED': '배송 취소'
        };
        return statusMap[status] || status;
    }

    getOrderActions(order) {
        let actions = '';
        
        if (order.status === 'CREATED' || order.status === 'PAID') {
            actions += `<button class="btn-small btn-cancel" onclick="app.cancelOrder(${order.id})">취소</button>`;
        }
        
        return actions;
    }

    getShipmentActions(shipment) {
        let actions = '';
        
        if (shipment.status === 'PREPARING') {
            actions += `<button class="btn-small btn-ship" onclick="app.shipOrder(${shipment.id})">배송 시작</button>`;
        }
        
        if (shipment.status === 'SHIPPED' || shipment.status === 'IN_TRANSIT') {
            actions += `<button class="btn-small btn-deliver" onclick="app.deliverOrder(${shipment.id})">배송 완료</button>`;
        }
        
        return actions;
    }

    addLog(message, type = 'info') {
        const logs = document.getElementById('logs');
        const logEntry = document.createElement('div');
        logEntry.className = `log-entry log-${type}`;
        logEntry.innerHTML = `
            <span class="log-time">[${new Date().toLocaleTimeString()}]</span>
            <span class="log-message">${message}</span>
        `;
        logs.appendChild(logEntry);
        logs.scrollTop = logs.scrollHeight;
    }

    clearLogs() {
        document.getElementById('logs').innerHTML = '';
        this.addLog('🗑️ 로그가 지워졌습니다', 'info');
    }
}

// 앱 초기화
const app = new RollbackDemo();