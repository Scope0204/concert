package hhplus.concert.domain.payment.event;

public interface PaymentEventPublisher {
    void publishPaymentEvent(PaymentEvent event);
}
