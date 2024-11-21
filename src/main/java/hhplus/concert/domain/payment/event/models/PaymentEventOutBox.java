package hhplus.concert.domain.payment.event.models;

import hhplus.concert.support.type.EventStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@Entity
public class PaymentEventOutBox {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "payment_id", nullable = false)
    private Long paymentId;

    @Column(name = "event_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private EventStatus eventStatus;

    @Column(name = "published_date_time", nullable = false)
    private LocalDateTime publishedDateTime;

    public PaymentEventOutBox(Long paymentId, EventStatus eventStatus) {
        this.paymentId = paymentId;
        this.eventStatus = eventStatus;
        this.publishedDateTime = LocalDateTime.now();
    }
    public void updateEventStatus(EventStatus eventStatus) {
        this.eventStatus = eventStatus;
    }
}
