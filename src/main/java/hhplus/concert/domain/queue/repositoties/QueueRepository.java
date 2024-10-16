package hhplus.concert.domain.queue.repositoties;

import hhplus.concert.support.type.QueueStatus;
import hhplus.concert.domain.queue.models.Queue;

public interface QueueRepository {
    int getQueuePositionInWaitingList(Long queueId, QueueStatus queueStatus);
    Queue findByUserIdAndStatus(Long userId, QueueStatus queueStatus);
    Queue findByToken(String token);
    void save(Queue queue);
}
