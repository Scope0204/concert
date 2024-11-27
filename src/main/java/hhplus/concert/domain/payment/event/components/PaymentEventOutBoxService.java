package hhplus.concert.domain.payment.event.components;

import hhplus.concert.domain.payment.event.models.PaymentEvent;
import hhplus.concert.domain.payment.event.models.PaymentEventOutBox;
import hhplus.concert.domain.payment.event.repositories.PaymentEventOutBoxRepository;
import hhplus.concert.support.type.EventStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
public class PaymentEventOutBoxService {

    private final PaymentEventOutBoxRepository paymentEventOutBoxRepository;
    private final PaymentRemoteEventPublisher paymentRemoteEventPublisher;

    public PaymentEventOutBoxService(PaymentEventOutBoxRepository paymentEventOutBoxRepository, PaymentRemoteEventPublisher paymentRemoteEventPublisher) {
        this.paymentEventOutBoxRepository = paymentEventOutBoxRepository;
        this.paymentRemoteEventPublisher = paymentRemoteEventPublisher;
    }

    /**
     * outbox 저장
     */
    public void saveEventOutBox(Long paymentId, EventStatus eventStatus) {
        PaymentEventOutBox paymentEventOutBox = new PaymentEventOutBox(paymentId, eventStatus);
        paymentEventOutBoxRepository.save(paymentEventOutBox);
    }

    /**
     *  Kafka event 발행.
     */
    public void publishPaymentEvent(PaymentEvent event) {
        paymentRemoteEventPublisher.publishPaymentEvent(event);
    }

    /**
     *  paymentId 로 paymentEventOutBox 를 조회 후, 변경 상태를 저장
     */
    public void updateEventStatus(Long paymentId, EventStatus eventStatus) {
        PaymentEventOutBox paymentEventOutBox = paymentEventOutBoxRepository.findByPaymentId(paymentId);
        paymentEventOutBox.updateEventStatus(eventStatus);
        paymentEventOutBoxRepository.save(paymentEventOutBox);
    }

    /**
     * 상태가 INIT 인 경우 실패로 간주(최초 저장 상태 값)
     * 10분이 지났음에도 발행이 실패된 EventOutBox 목록을 조회 후 재시도
     */
    public void retryFailedPaymentEvent() {
        List<PaymentEventOutBox> failedPaymentEventOutBoxList =  paymentEventOutBoxRepository.findAllFailedEvent(LocalDateTime.now().minusMinutes(10));
        for (PaymentEventOutBox paymentEventOutBox : failedPaymentEventOutBoxList) {
            paymentRemoteEventPublisher.publishPaymentEvent(new PaymentEvent(paymentEventOutBox.getPaymentId()));
        }
    }

    /**
     * 발행된 후 일주일이 지난 EventOutBox 를 삭제
     */
    @Transactional
    public void deletePublishedPaymentEvent() {
        try {
            paymentEventOutBoxRepository.deleteAllPublishedEvent(LocalDateTime.now().minusDays(7));
        } catch (Exception e) {
            log.error(String.valueOf(e));
        }
    }

}
