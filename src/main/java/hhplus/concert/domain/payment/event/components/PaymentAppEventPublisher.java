package hhplus.concert.domain.payment.event.components;

import hhplus.concert.domain.payment.event.models.PaymentEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class PaymentAppEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    public PaymentAppEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public void publishPaymentEvent(PaymentEvent event) {
        applicationEventPublisher.publishEvent(event);
    }
}
