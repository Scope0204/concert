package hhplus.concert.infra.jpa;

import hhplus.concert.domain.payment.event.models.PaymentEventOutBox;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaPaymentOutBoxRepository extends JpaRepository<PaymentEventOutBox, Long> {
    PaymentEventOutBox findByPaymentId(Long paymentId);
}

