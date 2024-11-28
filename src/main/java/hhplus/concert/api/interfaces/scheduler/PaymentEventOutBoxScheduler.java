package hhplus.concert.api.interfaces.scheduler;

import hhplus.concert.domain.payment.event.components.PaymentEventOutBoxService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PaymentEventOutBoxScheduler {
    private final PaymentEventOutBoxService paymentEventOutBoxService;

    public PaymentEventOutBoxScheduler(PaymentEventOutBoxService paymentEventOutBoxService) {
        this.paymentEventOutBoxService = paymentEventOutBoxService;
    }

    /**
     * 발행이 실패한 이벤트를 다시 재시도한다.
     */
    @Scheduled(fixedRate = 60000)
    public void retryFailedPaymentEvent() {
        log.info("Retry Failed Payment Event");
        paymentEventOutBoxService.retryFailedPaymentEvent();
    }

    /**
     * 발행이 완료된 이벤트를 삭제한다.
     */
    @Scheduled(fixedRate = 60000)
    public void deletePublishedPaymentEvent() {
        log.info("Delete Published Payment Event");
        paymentEventOutBoxService.deletePublishedPaymentEvent();
    }
}
