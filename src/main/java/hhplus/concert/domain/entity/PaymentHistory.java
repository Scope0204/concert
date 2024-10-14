package hhplus.concert.domain.entity;

import hhplus.concert.common.type.PaymentStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class PaymentHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_history_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private User user;

    @ManyToOne
    @JoinColumn(name = "payment_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Payment payment;

    @Column(name = "amount", nullable = false)
    private Long amount;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public PaymentHistory(User user, Payment payment, Long amount, PaymentStatus status, LocalDateTime createdAt) {
        this.user = user;
        this.payment = payment;
        this.amount = amount;
        this.status = status;
        this.createdAt = createdAt;
    }
}
