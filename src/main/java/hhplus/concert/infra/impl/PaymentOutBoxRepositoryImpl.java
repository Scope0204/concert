package hhplus.concert.infra.impl;

import hhplus.concert.domain.payment.event.models.PaymentEventOutBox;
import hhplus.concert.domain.payment.event.repositories.PaymentEventOutBoxRepository;
import hhplus.concert.infra.jpa.JpaPaymentOutBoxRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public class PaymentOutBoxRepositoryImpl implements PaymentEventOutBoxRepository {
    private final JpaPaymentOutBoxRepository jpaPaymentOutBoxRepository;

    public PaymentOutBoxRepositoryImpl(JpaPaymentOutBoxRepository jpaPaymentOutBoxRepository) {
        this.jpaPaymentOutBoxRepository = jpaPaymentOutBoxRepository;
    }

    @Override
    public void save(PaymentEventOutBox paymentEventOutBox) {
        jpaPaymentOutBoxRepository.save(paymentEventOutBox);
    }


    @Override
    public PaymentEventOutBox findByPaymentId(Long paymentId) {
        return jpaPaymentOutBoxRepository.findByPaymentId(paymentId);
    }

    @Override
    public List<PaymentEventOutBox> findAllFailedEvent(LocalDateTime dateTime) {
        return jpaPaymentOutBoxRepository.findAllFailedEvent(dateTime);
    }

    @Override
    public void deleteAllPublishedEvent(LocalDateTime dateTime) {
        jpaPaymentOutBoxRepository.deleteAllPublishedEvent(dateTime);
    }
}
