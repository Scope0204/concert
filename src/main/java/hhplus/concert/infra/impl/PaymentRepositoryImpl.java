package hhplus.concert.infra.impl;

import hhplus.concert.domain.payment.models.Payment;
import hhplus.concert.domain.payment.repositories.PaymentRepository;
import hhplus.concert.infra.jpa.JpaPaymentRepository;
import org.springframework.stereotype.Repository;

@Repository
public class PaymentRepositoryImpl implements PaymentRepository {
    private final JpaPaymentRepository jpaPaymentRepository;

    public PaymentRepositoryImpl(JpaPaymentRepository jpaPaymentRepository) {
        this.jpaPaymentRepository = jpaPaymentRepository;
    }

    @Override
    public void save(Payment payment) {
        jpaPaymentRepository.save(payment);
    }
}
