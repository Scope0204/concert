package hhplus.concert.domain.queue.models;

import hhplus.concert.domain.user.models.User;
import hhplus.concert.support.type.QueueStatus;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
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

    @Column(name = "entered_at")
    private LocalDateTime enteredAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    public Queue(User user, String token, QueueStatus status, LocalDateTime createdAt, LocalDateTime enteredAt, LocalDateTime updatedAt) {
        this.user = user;
        this.token = token;
        this.status = status;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now(); // 현재 시간으로 초기화
        this.enteredAt = enteredAt;
        this.updatedAt = updatedAt;
    }
    public void updateStatus(QueueStatus queueStatus) {
        this.status = queueStatus;
        this.updatedAt = LocalDateTime.now(); // 현재 시간으로 변경 사항 기록
    }

}
