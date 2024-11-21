package hhplus.concert.domain.payment.event.components;

import hhplus.concert.domain.payment.event.models.PaymentEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Qualifier("applicationPublisher")
public class ApplicationPaymentEventPublisher implements PaymentEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void publishPaymentEvent(PaymentEvent event) {
        applicationEventPublisher.publishEvent(event);
    }
}
