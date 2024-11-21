package hhplus.concert.domain.payment.event;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ApplicationPaymentEventPublisher implements PaymentEventPublisher{

    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void publishPaymentEvent(PaymentEvent event) {
        applicationEventPublisher.publishEvent(event);
    }
}
