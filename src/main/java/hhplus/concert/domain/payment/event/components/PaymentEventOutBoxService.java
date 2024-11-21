package hhplus.concert.domain.payment.event.components;

import hhplus.concert.domain.payment.event.models.PaymentEvent;
import hhplus.concert.domain.payment.event.models.PaymentEventOutBox;
import hhplus.concert.domain.payment.event.repositories.PaymentEventOutBoxRepository;
import hhplus.concert.support.type.EventStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PaymentEventOutBoxService {

    private final PaymentEventOutBoxRepository paymentEventOutBoxRepository;
    private final PaymentEventPublisher paymentEventPublisher;

    public PaymentEventOutBoxService(PaymentEventOutBoxRepository paymentEventOutBoxRepository, @Qualifier("kafkaPublisher") PaymentEventPublisher paymentEventPublisher) {
        this.paymentEventOutBoxRepository = paymentEventOutBoxRepository;
        this.paymentEventPublisher = paymentEventPublisher;
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
        paymentEventPublisher.publishPaymentEvent(event);
    }

    /**
     *  paymentId 로 paymentEventOutBox 를 조회 후, 변경 상태를 저장
     */
    public void updateEventStatus(Long paymentId, EventStatus eventStatus) {
        PaymentEventOutBox paymentEventOutBox = paymentEventOutBoxRepository.findByPaymentId(paymentId);
        paymentEventOutBox.updateEventStatus(eventStatus);
        paymentEventOutBoxRepository.save(paymentEventOutBox);
    }

}
