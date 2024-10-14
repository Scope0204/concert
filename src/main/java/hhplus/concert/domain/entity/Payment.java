package hhplus.concert.domain.entity;

import hhplus.concert.common.type.PaymentStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private User user;

    @ManyToOne
    @JoinColumn(name = "reservation_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Reservation reservation;

    @Column(name = "amount", nullable = false)
    private Long amount;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    @Column(name = "executed_at", nullable = false)
    private LocalDateTime executedAt;

    public Payment(User user, Reservation reservation, Long amount, PaymentStatus status, LocalDateTime executedAt) {
        this.user = user;
        this.reservation = reservation;
        this.amount = amount;
        this.status = status;
        this.executedAt = executedAt;
    }
}
