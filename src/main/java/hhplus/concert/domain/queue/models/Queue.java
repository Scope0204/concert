package hhplus.concert.domain.queue.models;

import hhplus.concert.support.type.QueueStatus;
import hhplus.concert.domain.user.models.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Builder
@Getter
@Entity
public class Queue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "queue_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private User user;

    @Column(name = "token", nullable = false)
    private String token;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private QueueStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "entered_at", nullable = false)
    private LocalDateTime enteredAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

   /* public Queue(User user, String token, QueueStatus status, LocalDateTime createdAt) {
        this.user = user;
        this.token = token;
        this.status = status;
        this.createdAt = createdAt;
    }*/

    public void updateStatus(QueueStatus queueStatus) {
        this.status = queueStatus;
    }
}
