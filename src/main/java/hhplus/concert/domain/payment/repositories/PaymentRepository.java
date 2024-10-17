package hhplus.concert.domain.payment.repositories;

import hhplus.concert.domain.payment.models.Payment;

public interface PaymentRepository {
    void save(Payment payment);
}
