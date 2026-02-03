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

