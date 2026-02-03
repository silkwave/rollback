-- 고객 테이블
CREATE TABLE IF NOT EXISTS customers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_number VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    status VARCHAR(20) DEFAULT 'ACTIVE', -- ACTIVE, INACTIVE, SUSPENDED, CLOSED
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    -- 추가 제약조건
    CONSTRAINT chk_customer_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED', 'CLOSED'))
);

-- 계좌 테이블
CREATE TABLE IF NOT EXISTS accounts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    account_number VARCHAR(30) NOT NULL UNIQUE,
    customer_id BIGINT NOT NULL,
    account_type VARCHAR(20) NOT NULL, -- CHECKING, SAVINGS, CREDIT, BUSINESS
    currency VARCHAR(10) NOT NULL DEFAULT 'KRW',
    balance DECIMAL(19,2) NOT NULL DEFAULT 0.00,
    status VARCHAR(20) DEFAULT 'ACTIVE', -- ACTIVE, FROZEN, CLOSED, SUSPENDED
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    last_transaction_at TIMESTAMP,
    account_holder_name VARCHAR(100) NOT NULL,
    FOREIGN KEY (customer_id) REFERENCES customers(id),
    -- 추가 제약조건
    CONSTRAINT chk_account_type CHECK (account_type IN ('CHECKING', 'SAVINGS', 'CREDIT', 'BUSINESS')),
    CONSTRAINT chk_account_status CHECK (status IN ('ACTIVE', 'FROZEN', 'CLOSED', 'SUSPENDED')),
    CONSTRAINT chk_balance_non_negative CHECK (balance >= 0)
);

-- 거래 내역 테이블
CREATE TABLE IF NOT EXISTS transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    guid VARCHAR(36) NOT NULL,
    from_account_id BIGINT,
    to_account_id BIGINT,
    customer_id BIGINT NOT NULL,
    transaction_type VARCHAR(20) NOT NULL, -- DEPOSIT, WITHDRAWAL, TRANSFER, FEE
    amount DECIMAL(19,2) NOT NULL,
    currency VARCHAR(10) NOT NULL DEFAULT 'KRW',
    description VARCHAR(500),
    status VARCHAR(20) DEFAULT 'PENDING', -- PENDING, COMPLETED, FAILED, CANCELLED
    reference_number VARCHAR(50),
    failure_reason VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    -- 은행 거래 필드 추가
    ip_address VARCHAR(45),
    device_info VARCHAR(100),
    created_by VARCHAR(50),
    approved_by VARCHAR(50),
    approved_at TIMESTAMP,
    transaction_channel VARCHAR(20) DEFAULT 'ONLINE', -- ONLINE, MOBILE, ATM, BRANCH
    transaction_category VARCHAR(30), -- TRANSFER, PAYMENT, UTILITY, LOAN, etc.
    fee_amount DECIMAL(19,2) DEFAULT 0.00,
    balance_after DECIMAL(19,2),
    FOREIGN KEY (from_account_id) REFERENCES accounts(id),
    FOREIGN KEY (to_account_id) REFERENCES accounts(id),
    FOREIGN KEY (customer_id) REFERENCES customers(id),
-- 추가 제약조건
    CONSTRAINT chk_transaction_type CHECK (transaction_type IN ('DEPOSIT', 'WITHDRAWAL', 'FEE', 'INTEREST', 'PENALTY')),
    CONSTRAINT chk_transaction_status CHECK (status IN ('PENDING', 'COMPLETED', 'FAILED', 'CANCELLED', 'REVERSED')),
    CONSTRAINT chk_amount_positive CHECK (amount > 0),
    CONSTRAINT chk_fee_non_negative CHECK (fee_amount >= 0),
    CONSTRAINT chk_transaction_channel CHECK (transaction_channel IN ('ONLINE', 'MOBILE', 'ATM', 'BRANCH', 'API')),
    CONSTRAINT uk_transaction_guid UNIQUE (guid)
);

-- 알림 로그 테이블 (기존과 호환)
CREATE TABLE IF NOT EXISTS notification_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    guid VARCHAR(36),
    message VARCHAR(1000),
    type VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 알림 로그 인덱스
CREATE INDEX idx_notification_logs_created_at ON notification_logs(created_at);

-- 초기 샘플 데이터
INSERT INTO customers (customer_number, name, email, phone_number) VALUES 
('CUST001', '김철수', 'kimcheolsu@example.com', '010-1234-5678'),
('CUST002', '이영희', 'leeyounghee@example.com', '010-2345-6789'),
('CUST003', '박상조', 'sangjo@example.com', '010-3456-7890');

INSERT INTO accounts (account_number, customer_id, account_type, currency, balance, account_holder_name) VALUES 
('ACC001', 1, 'CHECKING', 'KRW', 1000000.00, '김철수'),
('ACC002', 1, 'SAVINGS', 'KRW', 5000000.00, '김철수'),
('ACC003', 2, 'CHECKING', 'KRW', 2000000.00, '이영희'),
('ACC004', 3, 'BUSINESS', 'KRW', 10000000.00, '박상조');

-- 성능 최적화를 위한 인덱스 생성
-- 고객 관련 인덱스
CREATE INDEX idx_customers_customer_number ON customers(customer_number);
CREATE INDEX idx_customers_email ON customers(email);
CREATE INDEX idx_customers_status ON customers(status);

-- 계좌 관련 인덱스
CREATE INDEX idx_accounts_customer_id ON accounts(customer_id);
CREATE INDEX idx_accounts_account_number ON accounts(account_number);
CREATE INDEX idx_accounts_status ON accounts(status);
CREATE INDEX idx_accounts_account_type ON accounts(account_type);
CREATE INDEX idx_accounts_created_at ON accounts(created_at);

-- 거래 관련 인덱스
CREATE INDEX idx_transactions_from_account ON transactions(from_account_id);
CREATE INDEX idx_transactions_to_account ON transactions(to_account_id);
CREATE INDEX idx_transactions_customer ON transactions(customer_id);
CREATE INDEX idx_transactions_created_at ON transactions(created_at);
CREATE INDEX idx_transactions_status ON transactions(status);
CREATE INDEX idx_transactions_type ON transactions(transaction_type);
CREATE INDEX idx_transactions_guid ON transactions(guid);
CREATE INDEX idx_transactions_reference ON transactions(reference_number);

-- 알림 로그 인덱스
CREATE INDEX idx_notification_logs_created_at ON notification_logs(created_at);