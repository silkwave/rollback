-- 고객 테이블
CREATE TABLE IF NOT EXISTS customers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY, -- 고객 고유 ID
    customer_number VARCHAR(20) NOT NULL UNIQUE, -- 고객 식별 번호 (고유)
    name VARCHAR(100) NOT NULL, -- 고객 이름
    email VARCHAR(150) NOT NULL, -- 고객 이메일 주소
    phone_number VARCHAR(20) NOT NULL, -- 고객 전화번호
    status VARCHAR(20) DEFAULT 'ACTIVE', -- 고객 상태 (ACTIVE, INACTIVE, SUSPENDED, CLOSED)
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- 생성 일시
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, -- 최종 수정 일시
    -- 추가 제약조건
    CONSTRAINT chk_customer_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED', 'CLOSED'))
);
COMMENT ON TABLE customers IS '은행 고객 정보 테이블';
COMMENT ON COLUMN customers.id IS '고객 고유 식별자';
COMMENT ON COLUMN customers.customer_number IS '고객 식별 번호';
COMMENT ON COLUMN customers.name IS '고객 이름';
COMMENT ON COLUMN customers.email IS '고객 이메일';
COMMENT ON COLUMN customers.phone_number IS '고객 전화번호';
COMMENT ON COLUMN customers.status IS '고객 상태';
COMMENT ON COLUMN customers.created_at IS '고객 정보 생성 일시';
COMMENT ON COLUMN customers.updated_at IS '고객 정보 최종 수정 일시';


-- 계좌 테이블
CREATE TABLE IF NOT EXISTS accounts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY, -- 계좌 고유 ID
    account_number VARCHAR(30) NOT NULL UNIQUE, -- 계좌 번호 (고유)
    customer_id BIGINT NOT NULL, -- 고객 ID (FK)
    account_type VARCHAR(20) NOT NULL, -- 계좌 유형 (CHECKING, SAVINGS, CREDIT, BUSINESS)
    currency VARCHAR(10) NOT NULL DEFAULT 'KRW', -- 통화 코드
    balance DECIMAL(19,2) NOT NULL DEFAULT 0.00, -- 현재 잔액
    status VARCHAR(20) DEFAULT 'ACTIVE', -- 계좌 상태 (ACTIVE, FROZEN, CLOSED, SUSPENDED)
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- 생성 일시
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, -- 최종 수정 일시
    last_transaction_at TIMESTAMP, -- 마지막 거래 일시
    account_holder_name VARCHAR(100) NOT NULL, -- 계좌주 이름
    FOREIGN KEY (customer_id) REFERENCES customers(id),
    -- 추가 제약조건
    CONSTRAINT chk_account_type CHECK (account_type IN ('CHECKING', 'SAVINGS', 'CREDIT', 'BUSINESS')),
    CONSTRAINT chk_account_status CHECK (status IN ('ACTIVE', 'FROZEN', 'CLOSED', 'SUSPENDED')),
    CONSTRAINT chk_balance_non_negative CHECK (balance >= 0)
);
COMMENT ON TABLE accounts IS '은행 계좌 정보 테이블';
COMMENT ON COLUMN accounts.id IS '계좌 고유 식별자';
COMMENT ON COLUMN accounts.account_number IS '계좌 번호';
COMMENT ON COLUMN accounts.customer_id IS '고객 고유 식별자 (외래 키)';
COMMENT ON COLUMN accounts.account_type IS '계좌 유형';
COMMENT ON COLUMN accounts.currency IS '통화';
COMMENT ON COLUMN accounts.balance IS '현재 계좌 잔액';
COMMENT ON COLUMN accounts.status IS '계좌 상태';
COMMENT ON COLUMN accounts.created_at IS '계좌 생성 일시';
COMMENT ON COLUMN accounts.updated_at IS '계좌 정보 최종 수정 일시';
COMMENT ON COLUMN accounts.last_transaction_at IS '마지막 거래 발생 일시';
COMMENT ON COLUMN accounts.account_holder_name IS '계좌주 명';


-- 거래 내역 테이블
CREATE TABLE IF NOT EXISTS transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY, -- 거래 고유 ID
    guid VARCHAR(36) NOT NULL UNIQUE, -- 거래 고유 식별자 (UUID)
    from_account_id BIGINT, -- 출금 계좌 ID (FK, null 가능)
    to_account_id BIGINT, -- 입금 계좌 ID (FK, null 가능)
    customer_id BIGINT NOT NULL, -- 고객 ID (FK)
    transaction_type VARCHAR(20) NOT NULL, -- 거래 유형 (DEPOSIT, WITHDRAWAL, TRANSFER 등)
    amount DECIMAL(19,2) NOT NULL, -- 거래 금액
    currency VARCHAR(10) NOT NULL DEFAULT 'KRW', -- 통화 코드
    description VARCHAR(500), -- 거래 설명
    status VARCHAR(20) DEFAULT 'PENDING', -- 거래 상태 (PENDING, COMPLETED, FAILED, CANCELLED)
    failure_reason VARCHAR(500), -- 실패 사유
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- 거래 생성 일시
    completed_at TIMESTAMP, -- 거래 완료/실패/취소 일시
    FOREIGN KEY (from_account_id) REFERENCES accounts(id),
    FOREIGN KEY (to_account_id) REFERENCES accounts(id),
    FOREIGN KEY (customer_id) REFERENCES customers(id),
-- 추가 제약조건
    CONSTRAINT chk_transaction_type CHECK (transaction_type IN ('DEPOSIT', 'WITHDRAWAL', 'FEE', 'INTEREST', 'PENALTY')),
    CONSTRAINT chk_transaction_status CHECK (status IN ('PENDING', 'COMPLETED', 'FAILED', 'CANCELLED', 'REVERSED')),
    CONSTRAINT chk_amount_positive CHECK (amount > 0)
);
COMMENT ON TABLE transactions IS '은행 거래 내역 테이블';
COMMENT ON COLUMN transactions.id IS '거래 고유 식별자';
COMMENT ON COLUMN transactions.guid IS '글로벌 고유 식별자';
COMMENT ON COLUMN transactions.from_account_id IS '출금 계좌 ID (이체 시)';
COMMENT ON COLUMN transactions.to_account_id IS '입금 계좌 ID (이체 및 입금 시)';
COMMENT ON COLUMN transactions.customer_id IS '거래를 요청한 고객 ID';
COMMENT ON COLUMN transactions.transaction_type IS '거래 유형';
COMMENT ON COLUMN transactions.amount IS '거래 금액';
COMMENT ON COLUMN transactions.currency IS '거래 통화';
COMMENT ON COLUMN transactions.description IS '거래 상세 설명';
COMMENT ON COLUMN transactions.status IS '거래 처리 상태';
COMMENT ON COLUMN transactions.failure_reason IS '거래 실패 사유';
COMMENT ON COLUMN transactions.created_at IS '거래 생성 일시';
COMMENT ON COLUMN transactions.completed_at IS '거래 처리 완료 일시';


-- 알림 로그 테이블
CREATE TABLE IF NOT EXISTS notification_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY, -- 알림 로그 고유 ID
    guid VARCHAR(36), -- 알림 관련 GUID (예: 거래 GUID)
    message VARCHAR(4000), -- 알림 메시지 내용 (관련 ID 정보 포함)
    type VARCHAR(50), -- 알림 유형 (SUCCESS, FAILURE, INFO, WARNING)
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP -- 알림 생성 일시
);
COMMENT ON TABLE notification_logs IS '시스템 알림 로그 테이블';
COMMENT ON COLUMN notification_logs.id IS '알림 로그 고유 식별자';
COMMENT ON COLUMN notification_logs.guid IS '알림 관련 글로벌 고유 식별자';
COMMENT ON COLUMN notification_logs.message IS '알림 메시지 본문';
COMMENT ON COLUMN notification_logs.type IS '알림 유형';
COMMENT ON COLUMN notification_logs.created_at IS '알림 생성 일시';


-- 성능 최적화를 위한 인덱스 생성
-- 고객 관련 인덱스
CREATE INDEX idx_customers_customer_number ON customers(customer_number); -- 고객 번호 검색 최적화
CREATE INDEX idx_customers_email ON customers(email); -- 고객 이메일 검색 최적화
CREATE INDEX idx_customers_status ON customers(status); -- 고객 상태별 검색 최적화

-- 계좌 관련 인덱스
CREATE INDEX idx_accounts_customer_id ON accounts(customer_id); -- 고객 ID별 계좌 검색 최적화
CREATE INDEX idx_accounts_account_number ON accounts(account_number); -- 계좌 번호 검색 최적화
CREATE INDEX idx_accounts_status ON accounts(status); -- 계좌 상태별 검색 최적화
CREATE INDEX idx_accounts_account_type ON accounts(account_type); -- 계좌 유형별 검색 최적화
CREATE INDEX idx_accounts_created_at ON accounts(created_at); -- 계좌 생성일시별 검색 최적화

-- 거래 관련 인덱스
CREATE INDEX idx_transactions_from_account ON transactions(from_account_id); -- 출금 계좌별 거래 검색 최적화
CREATE INDEX idx_transactions_to_account ON transactions(to_account_id); -- 입금 계좌별 거래 검색 최적화
CREATE INDEX idx_transactions_customer ON transactions(customer_id); -- 고객 ID별 거래 검색 최적화
CREATE INDEX idx_transactions_created_at ON transactions(created_at); -- 거래 생성일시별 검색 최적화
CREATE INDEX idx_transactions_status ON transactions(status); -- 거래 상태별 검색 최적화
CREATE INDEX idx_transactions_type ON transactions(transaction_type); -- 거래 유형별 검색 최적화
CREATE INDEX idx_transactions_guid ON transactions(guid); -- 거래 GUID 검색 최적화

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