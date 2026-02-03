package com.example.rollback.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 계좌 개설 요청 DTO 클래스
 * 
 * <p>
 * 새로운 계좌를 개설하기 위해 필요한 정보를 담고 있는 데이터 전송 객체입니다.
 * 클라이언트로부터 계좌 개설 요청을 받아 Account 엔티티로 변환하는 역할을 합니다.
 * </p>
 * 
 * <p>
 * 주요 기능:
 * </p>
 * <ul>
 * <li>계좌 개설 정보 유효성 검사</li>
 * <li>Account 엔티티 변환</li>
 * <li>테스트용 실패 강제 기능</li>
 * </ul>
 * 
 * @author Banking System Team
 * @version 1.0
 * @since 2024-01-01
 */
@Data
@Slf4j
public class AccountRequest {

    /** 계좌 소유자 고객 ID (필수) */
    @NotNull(message = "고객 ID는 필수입니다")
    private Long customerId;

    /** 계좌 유형 (필수) - CHECKING, SAVINGS, CREDIT, BUSINESS */
    @NotBlank(message = "계좌 유형은 필수입니다")
    private String accountType;

    /** 통화 코드 (필수) - KRW, USD, EUR 등 */
    @NotBlank(message = "통화는 필수입니다")
    private String currency;

    /** 계좌주 성명 (필수) */
    @NotBlank(message = "계좌주 명은 필수입니다")
    private String accountHolderName;

    /** 지점 코드 (선택사항) */
    private String branchCode;

    /** 초기 입금액 (선택사항) */
    private BigDecimal initialDeposit;

    /** 테스트용 실패 강제 여부 */
    private boolean forceFailure = false;

    /**
     * AccountRequest를 Account 엔티티로 변환합니다
     * 
     * @param accountNumber 생성될 계좌번호
     * @return 변환된 Account 엔티티
     */
    public Account toAccount(String accountNumber) {
        log.debug("AccountRequest to Account - customerId: {}, accountType: {}, currency: {}, initialDeposit: {}",
                customerId, accountType, currency, initialDeposit);

        AccountType accType = AccountType.valueOf(accountType.toUpperCase());
        Account account = Account.create(accountNumber, customerId, accType, currency, initialDeposit,
                accountHolderName);

        // 지점 코드가 제공된 경우 설정
        if (branchCode != null && !branchCode.trim().isEmpty()) {
            account.setBranchCode(branchCode);
        }

        return account;
    }

    /**
     * 계좌번호를 반환합니다 (서비스에서 설정)
     * 
     * @return 계좌번호 (항상 null 반환)
     */
    public String getAccountNumber() {
        return null;
    }

    /**
     * 유효성 검사 실패 시 알림 로그 객체를 생성합니다
     * 
     * @param guid         요청 GUID
     * @param errorMessage 에러 메시지
     * @return 생성된 NotificationLog 객체
     */
    public NotificationLog toErrorLog(String guid, String errorMessage) {
        // Embed customerId into the message
        String fullMessage = String.format("고객ID: %d - %s", customerId, errorMessage);
        
        // Use the constructor that takes guid, message, and type
        NotificationLog notificationLog = new NotificationLog(guid, fullMessage, NotificationLog.NotificationType.FAILURE);

        log.info("유효성 검사 오류 로그 생성 - GUID: {}, 고객ID: {}, 메시지: {}", guid, customerId, fullMessage);
        return notificationLog;
    }
}