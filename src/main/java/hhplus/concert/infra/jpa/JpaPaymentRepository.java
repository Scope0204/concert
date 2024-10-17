package hhplus.concert.infra.jpa;

import hhplus.concert.domain.payment.models.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaPaymentRepository extends JpaRepository<Payment, Long> {
}

