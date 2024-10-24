package hhplus.concert.domain.balance.models;

import hhplus.concert.domain.user.models.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Getter
@NoArgsConstructor
@Entity
public class Balance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "balance_id")
    private Long id;

    @Version
    private Long version;

    @ManyToOne
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private User user;

    @Column(nullable = false)
    private int amount;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Balance(User user, int amount, LocalDateTime updatedAt) {
        this.user = user;
        this.amount = amount;
        this.updatedAt = updatedAt;
    }

    public void updateAmount(int amount){
        this.amount += amount;
        this.updatedAt = LocalDateTime.now();
    }
}
