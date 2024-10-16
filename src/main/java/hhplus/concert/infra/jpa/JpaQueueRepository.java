package hhplus.concert.infra.jpa;

import hhplus.concert.support.type.QueueStatus;
import hhplus.concert.domain.queue.models.Queue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JpaQueueRepository extends JpaRepository<Queue, Long> {
    @Query("SELECT COUNT(q) FROM Queue q WHERE q.status = :queueStatus AND q.id < :queueId")
    int getQueuePositionInWaitingList(@Param("queueId") Long queueId, @Param("queueStatus") QueueStatus queueStatus);

    @Query("SELECT q FROM Queue q WHERE q.user.id = :userId AND q.status = :queueStatus")
    Queue findByUserIdAndStatus(@Param("userId") Long userId, @Param("queueStatus") QueueStatus queueStatus);

    Queue findByToken(String token);
}
