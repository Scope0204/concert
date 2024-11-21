package hhplus.concert.domain.payment.event.components;

import hhplus.concert.domain.payment.event.models.PaymentEvent;

public interface PaymentEventPublisher {
    void publishPaymentEvent(PaymentEvent event);
}
