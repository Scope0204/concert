package hhplus.concert.infra.jpa;

import hhplus.concert.domain.queue.models.Queue;
import hhplus.concert.support.type.QueueStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface JpaQueueRepository extends JpaRepository<Queue, Long> {

    @Modifying
    @Query("UPDATE Queue q SET q.status = :queueStatus, q.updatedAt = CURRENT_TIMESTAMP, q.enteredAt = CURRENT_TIMESTAMP WHERE q.id IN :queueIds")
    void updateQueuesToActive(@Param("queueIds") List<Long> queueIds, @Param("queueStatus") QueueStatus queueStatus);

    @Query("SELECT q FROM Queue q WHERE q.status = :queueStatus ORDER BY q.id ASC LIMIT :limit")
    List<Queue> findTopByStatusOrderByIdAsc(@Param("queueStatus") QueueStatus queueStatus, @Param("limit") int limit);

    @Query("SELECT COUNT(q) FROM Queue q WHERE q.status = :queueStatus")
    int getQueueCountByStatus(@Param("queueStatus") QueueStatus queueStatus);

    @Query("SELECT COUNT(q) FROM Queue q WHERE q.status = :queueStatus AND q.id < :queueId")
    int getQueuePositionInWaitingList(@Param("queueId") Long queueId, @Param("queueStatus") QueueStatus queueStatus);

    @Query("SELECT q FROM Queue q WHERE q.user.id = :userId AND q.status = :queueStatus")
    Queue findByUserIdAndStatus(@Param("userId") Long userId, @Param("queueStatus") QueueStatus queueStatus);

    Optional<Queue> findByToken(String token);
}
