package hhplus.concert.domain.payment.event.components;

import hhplus.concert.domain.payment.event.models.PaymentEvent;
import hhplus.concert.support.type.EventStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
public class PaymentEventListener {
    private final PaymentEventOutBoxService paymentEventOutBoxService;

    public PaymentEventListener(PaymentEventOutBoxService paymentEventOutBoxService) {
        this.paymentEventOutBoxService = paymentEventOutBoxService;
    }

    /**
     * outbox 저장.
     * 1. 커밋 전에 outbox 가 저장 되었으므로, 트랜잭션이 실패했어도 outbox 는 Init 상태로 저장이 된다.
     * 2. 만일 트랜잭션이 실패한 경우의 outbox 는 스케줄러를 통해 재시도를 하도록 한다.
     */
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void savePaymentEventOutBox (PaymentEvent event) {
        log.info("PaymentEventOutBox 저장. PaymentId = {} ", event.getPaymentId());
        paymentEventOutBoxService.saveEventOutBox(event.getPaymentId(), EventStatus.INIT);
    }


    /**
     * outbox 상태를 변경하고, kafka event 를 발행
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publishPaymentEvent(PaymentEvent event) {
        paymentEventOutBoxService.publishPaymentEvent(event);
    }
}
