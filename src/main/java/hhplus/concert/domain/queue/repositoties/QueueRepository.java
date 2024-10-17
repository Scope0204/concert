package hhplus.concert.domain.queue.repositoties;

import hhplus.concert.domain.queue.models.Queue;
import hhplus.concert.support.type.QueueStatus;

import java.util.List;

public interface QueueRepository {
    void updateQueuesToActive(List<Long> queueIds, QueueStatus queueStatus);
    List<Queue> findTopByStatusOrderByIdAsc(QueueStatus queueStatus, int limit);
    int getQueueCountByStatus(QueueStatus queueStatus);
    int getQueuePositionInWaitingList(Long queueId, QueueStatus queueStatus);
    Queue findByUserIdAndStatus(Long userId, QueueStatus queueStatus);
    Queue findByToken(String token);
    void save(Queue queue);
}
