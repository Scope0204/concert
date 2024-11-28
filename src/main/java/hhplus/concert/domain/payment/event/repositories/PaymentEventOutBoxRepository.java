package hhplus.concert.domain.payment.event.repositories;

import hhplus.concert.domain.payment.event.models.PaymentEventOutBox;

import java.time.LocalDateTime;
import java.util.List;

public interface PaymentEventOutBoxRepository {
    void save(PaymentEventOutBox paymentEventOutBox);
    PaymentEventOutBox findByPaymentId(Long paymentId);
    List<PaymentEventOutBox> findAllFailedEvent(LocalDateTime dateTime);
    void deleteAllPublishedEvent(LocalDateTime dateTime);
}
