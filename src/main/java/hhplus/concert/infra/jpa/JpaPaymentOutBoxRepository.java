package hhplus.concert.infra.jpa;

import hhplus.concert.domain.payment.event.models.PaymentEventOutBox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface JpaPaymentOutBoxRepository extends JpaRepository<PaymentEventOutBox, Long> {
    PaymentEventOutBox findByPaymentId(Long paymentId);

    @Query("select peo from PaymentEventOutBox peo where peo.eventStatus = 'INIT' and peo.publishedAt < :dateTime")
    List<PaymentEventOutBox> findAllFailedEvent(LocalDateTime dateTime);

    @Modifying
    @Query("delete from PaymentEventOutBox peo where peo.eventStatus = 'PUBLISHED' and peo.publishedAt < :dateTime")
    void deleteAllPublishedEvent(LocalDateTime dateTime);
}

