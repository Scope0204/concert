package hhplus.concert.domain.payment.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
public class PaymentEventListener {

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(PaymentEvent event) {
        try {
            log.info("결제 데이터 전송: {}", event.getPaymentId());
            // TODO: 결제 정보를 타 플랫폼에 전달
        } catch (Exception e) {
            log.error("데이터 플랫폼 전송 실패: {}", e.getMessage());
            // TODO: 재시도 로직 추가 필요
        }
    }
}
