package hhplus.concert.domain.entity;

import hhplus.concert.common.type.QueueStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;

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
    private LocalDateTime entered_at;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updated_at;

    public Queue(User user, String token, QueueStatus status, LocalDateTime createdAt, LocalDateTime entered_at, LocalDateTime updated_at) {
        this.user = user;
        this.token = token;
        this.status = status;
        this.createdAt = createdAt;
        this.entered_at = entered_at;
        this.updated_at = updated_at;
    }
}
