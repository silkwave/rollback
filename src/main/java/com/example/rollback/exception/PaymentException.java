package com.example.rollback.exception;

/**
 * 결제 처리 중 발생하는 비즈니스 예외를 나타내는 커스텀 예외 클래스
 * 
 * <p>이 예외 클래스는 결제 관련 비즈니스 로직에서 발생하는
 * 다양한 예외 상황을 체계적으로 처리하기 위해 사용됩니다.</p>
 * 
 * <p>주요 사용 시나리오:</p>
 * <ul>
 *   <li>결제 금액 불일치 또는 유효하지 않은 금액</li>
 *   <li>결제 처리 중 외부 결제 게이트웨이 오류</li>
 *   <li>결제 상태의 부적절한 변경 시도</li>
 *   <li>잔액 부족 또한 한도 초과 등 재정 관련 문제</li>
 *   <li>결제 정보 위변조 또는 보안 관련 이슈</li>
 * </ul>
 * 
 * <p>{@link RuntimeException}을 상속받아 체크되지 않은 예외로
 * 구현되어, 컴파일 시점에서의 예외 처리 강제화 없이
 * 필요한 곳에서 선택적으로 처리할 수 있도록 설계되었습니다.</p>
 * 
 * <p>이 예외는 보통 {@code @Transactional}이 적용된 서비스 메서드에서
 * 발생하여 트랜잭션 롤백을 유발하는 데 사용됩니다.</p>
 * 
 * @author Spring Application Team
 * @version 1.0
 * @since 2024.01.01
 */
public class PaymentException extends RuntimeException {
    
    /**
     * 상세 메시지를 갖는 결제 예외를 생성합니다.
     * 
     * <p>이 생성자는 예외 발생 원인을 설명하는 상세 메시지를 받아
     * 결제 관련 예외 객체를 생성합니다. 메시지는 주로 클라이언트에게
     * 전달될 사용자 친화적인 형식으로 작성되어야 합니다.</p>
     * 
     * <p>사용 예시:</p>
     * <pre>{@code
     * throw new PaymentException("결제 금액이 주문 금액과 일치하지 않습니다.");
     * throw new PaymentException("결제 한도를 초과하였습니다.");
     * }</pre>
     * 
     * @param message 예외 발생 원인을 설명하는 상세 메시지.
     *               로깅 및 클라이언트 응답에 사용됩니다.
     */
    public PaymentException(String message) {
        super(message);
    }
    
    /**
     * 상세 메시지와 원인 예외를 갖는 결제 예외를 생성합니다.
     * 
     * <p>이 생성자는 예외 발생의 직접적인 원인이 된 다른 예외를
     * 함께 포함하여 예외 체인을 구성합니다. 이를 통해 원본 예외의
     * 스택 트레이스 정보를 유지하면서 결제 관련 예외로 변환할 수 있습니다.</p>
     * 
     * <p>이 생성자는 주로 외부 시스템(결제 게이트웨이 등)과의
     * 통신 중 발생한 예외를 결제 도메인의 예외로 변환할 때 사용됩니다.</p>
     * 
     * <p>사용 예시:</p>
     * <pre>{@code
     * try {
     *     paymentGateway.process(payment);
     * } catch (GatewayException e) {
     *     throw new PaymentException("결제 게이트웨이 통신 오류", e);
     * }
     * }</pre>
     * 
     * @param message 예외 발생 원인을 설명하는 상세 메시지.
     *               사용자에게 표시될 메시지입니다.
     * @param cause 이 예외를 발생시킨 원인이 되는 다른 예외 객체.
     *              원본 예외 정보가 보존되어 디버깅에 도움이 됩니다.
     */
    public PaymentException(String message, Throwable cause) {
        super(message, cause);
    }
}