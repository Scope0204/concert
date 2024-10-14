package hhplus.concert.domain.balance.models;

import hhplus.concert.domain.user.models.User;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class Balance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "balance_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private User user;

    @Column(nullable = false)
    private Long amount;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Balance(User user, Long amount, LocalDateTime updatedAt) {
        this.user = user;
        this.amount = amount;
        this.updatedAt = updatedAt;
    }
}
