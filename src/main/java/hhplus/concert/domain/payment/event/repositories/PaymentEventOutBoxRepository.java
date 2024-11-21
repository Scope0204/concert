package hhplus.concert.domain.payment.event.repositories;

import hhplus.concert.domain.payment.event.models.PaymentEventOutBox;

public interface PaymentEventOutBoxRepository {
    void save(PaymentEventOutBox paymentEventOutBox);
    PaymentEventOutBox findByPaymentId(Long paymentId);
}
