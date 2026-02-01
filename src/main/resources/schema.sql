-- 고객 테이블
CREATE TABLE IF NOT EXISTS customers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_number VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    date_of_birth DATE,
    gender VARCHAR(10),
    id_number VARCHAR(50),
    customer_type VARCHAR(20) DEFAULT 'INDIVIDUAL', -- INDIVIDUAL, BUSINESS
    risk_level VARCHAR(20) DEFAULT 'LOW', -- LOW, MEDIUM, HIGH
    status VARCHAR(20) DEFAULT 'ACTIVE', -- ACTIVE, INACTIVE, SUSPENDED, CLOSED
    address VARCHAR(500),
    city VARCHAR(100),
    country VARCHAR(100),
    postal_code VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP,
    -- 추가 제약조건
    CONSTRAINT chk_customer_type CHECK (customer_type IN ('INDIVIDUAL', 'BUSINESS')),
    CONSTRAINT chk_risk_level CHECK (risk_level IN ('LOW', 'MEDIUM', 'HIGH')),
    CONSTRAINT chk_customer_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED', 'CLOSED')),
    CONSTRAINT chk_email_format CHECK (email LIKE '%_@_%.%')
);

-- 계좌 테이블
CREATE TABLE IF NOT EXISTS accounts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    account_number VARCHAR(30) NOT NULL UNIQUE,
    customer_id BIGINT NOT NULL,
    account_type VARCHAR(20) NOT NULL, -- CHECKING, SAVINGS, CREDIT
    currency VARCHAR(10) NOT NULL DEFAULT 'KRW',
    balance DECIMAL(19,2) NOT NULL DEFAULT 0.00,
    overdraft_limit DECIMAL(19,2) NOT NULL DEFAULT 0.00,
    status VARCHAR(20) DEFAULT 'ACTIVE', -- ACTIVE, FROZEN, CLOSED, SUSPENDED
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    last_transaction_at TIMESTAMP,
    -- 은행 시스템 필드 추가
    daily_transaction_limit DECIMAL(19,2) DEFAULT 1000000.00,
    monthly_transaction_limit DECIMAL(19,2) DEFAULT 5000000.00,
    daily_transaction_amount DECIMAL(19,2) DEFAULT 0.00,
    monthly_transaction_amount DECIMAL(19,2) DEFAULT 0.00,
    last_daily_reset TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_monthly_reset TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    account_holder_name VARCHAR(100) NOT NULL,
    branch_code VARCHAR(10),
    FOREIGN KEY (customer_id) REFERENCES customers(id),
    -- 추가 제약조건
    CONSTRAINT chk_account_type CHECK (account_type IN ('CHECKING', 'SAVINGS', 'CREDIT', 'BUSINESS')),
    CONSTRAINT chk_account_status CHECK (status IN ('ACTIVE', 'FROZEN', 'CLOSED', 'SUSPENDED')),
    CONSTRAINT chk_balance_non_negative CHECK (balance >= 0),
    CONSTRAINT chk_overdraft_limit CHECK (overdraft_limit >= 0),
    CONSTRAINT chk_daily_limit_positive CHECK (daily_transaction_limit >= 0),
    CONSTRAINT chk_monthly_limit_positive CHECK (monthly_transaction_limit >= 0)
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
    CONSTRAINT chk_transaction_type CHECK (transaction_type IN ('DEPOSIT', 'WITHDRAWAL', 'TRANSFER', 'FEE', 'INTEREST', 'PENALTY')),
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
    account_id BIGINT,
    transaction_id BIGINT,
    customer_id BIGINT,
    message VARCHAR(255),
    type VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (account_id) REFERENCES accounts(id),
    FOREIGN KEY (transaction_id) REFERENCES transactions(id),
    FOREIGN KEY (customer_id) REFERENCES customers(id)
);

-- 초기 샘플 데이터
INSERT INTO customers (customer_number, name, email, phone_number, date_of_birth, id_number, customer_type, address, city, country, postal_code) VALUES 
('CUST001', '김철수', 'kimcheolsu@example.com', '010-1234-5678', '1990-01-15', '900101-1234567', 'INDIVIDUAL', '서울시 강남구 테헤란로 123', '서울', '대한민국', '06123'),
('CUST002', '이영희', 'leeyounghee@example.com', '010-2345-6789', '1985-05-20', '850501-2345678', 'INDIVIDUAL', '서울시 서초구 강남대로 456', '서울', '대한민국', '06456'),
('CUST003', '박상조', 'sangjo@example.com', '010-3456-7890', NULL, '123-45-67890', 'BUSINESS', '서울시 종로구 세종대로 789', '서울', '대한민국', '03123');

INSERT INTO accounts (account_number, customer_id, account_type, currency, balance, overdraft_limit, account_holder_name) VALUES 
('ACC001', 1, 'CHECKING', 'KRW', 1000000.00, 0.00, '김철수'),
('ACC002', 1, 'SAVINGS', 'KRW', 5000000.00, 0.00, '김철수'),
('ACC003', 2, 'CHECKING', 'KRW', 2000000.00, 500000.00, '이영희'),
('ACC004', 3, 'BUSINESS', 'KRW', 10000000.00, 2000000.00, '박상조');

-- 계좌 감사 로그 테이블
CREATE TABLE IF NOT EXISTS account_audit_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    account_id BIGINT NOT NULL,
    operation_type VARCHAR(20) NOT NULL, -- INSERT, UPDATE, DELETE, FREEZE, ACTIVATE
    old_values JSON,
    new_values JSON,
    changed_by VARCHAR(50),
    changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ip_address VARCHAR(45),
    session_id VARCHAR(100),
    reason VARCHAR(500),
    FOREIGN KEY (account_id) REFERENCES accounts(id),
    CONSTRAINT chk_audit_operation CHECK (operation_type IN ('INSERT', 'UPDATE', 'DELETE', 'FREEZE', 'ACTIVATE', 'CLOSE', 'SUSPEND'))
);

-- 거래 감사 로그 테이블
CREATE TABLE IF NOT EXISTS transaction_audit_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    transaction_id BIGINT NOT NULL,
    account_id BIGINT,
    operation_type VARCHAR(20) NOT NULL, -- CREATE, COMPLETE, FAIL, CANCEL, REVERSE
    old_status VARCHAR(20),
    new_status VARCHAR(20),
    changed_by VARCHAR(50),
    changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ip_address VARCHAR(45),
    session_id VARCHAR(100),
    reason VARCHAR(500),
    FOREIGN KEY (transaction_id) REFERENCES transactions(id),
    FOREIGN KEY (account_id) REFERENCES accounts(id),
    CONSTRAINT chk_txn_audit_operation CHECK (operation_type IN ('CREATE', 'COMPLETE', 'FAIL', 'CANCEL', 'REVERSE', 'APPROVE', 'REJECT'))
);

-- 로그인 기록 테이블
CREATE TABLE IF NOT EXISTS login_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    login_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ip_address VARCHAR(45),
    user_agent VARCHAR(500),
    login_status VARCHAR(20) NOT NULL, -- SUCCESS, FAILED, LOCKED
    failure_reason VARCHAR(200),
    session_id VARCHAR(100),
    FOREIGN KEY (customer_id) REFERENCES customers(id),
    CONSTRAINT chk_login_status CHECK (login_status IN ('SUCCESS', 'FAILED', 'LOCKED', 'SUSPICIOUS'))
);

-- 성능 최적화를 위한 인덱스 생성
-- 고객 관련 인덱스
CREATE INDEX idx_customers_customer_number ON customers(customer_number);
CREATE INDEX idx_customers_email ON customers(email);
CREATE INDEX idx_customers_status ON customers(status);
CREATE INDEX idx_customers_risk_level ON customers(risk_level);

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

-- 감사 로그 인덱스
CREATE INDEX idx_account_audit_account_id ON account_audit_log(account_id);
CREATE INDEX idx_account_audit_changed_at ON account_audit_log(changed_at);
CREATE INDEX idx_account_audit_operation ON account_audit_log(operation_type);

CREATE INDEX idx_transaction_audit_transaction_id ON transaction_audit_log(transaction_id);
CREATE INDEX idx_transaction_audit_account_id ON transaction_audit_log(account_id);
CREATE INDEX idx_transaction_audit_changed_at ON transaction_audit_log(changed_at);

CREATE INDEX idx_login_logs_customer_id ON login_logs(customer_id);
CREATE INDEX idx_login_logs_login_at ON login_logs(login_at);
CREATE INDEX idx_login_logs_status ON login_logs(login_status);
CREATE INDEX idx_login_logs_ip_address ON login_logs(ip_address);

