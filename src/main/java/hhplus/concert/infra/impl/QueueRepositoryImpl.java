package hhplus.concert.infra.impl;

import hhplus.concert.support.type.QueueStatus;
import hhplus.concert.infra.jpa.JpaQueueRepository;
import hhplus.concert.domain.queue.models.Queue;
import hhplus.concert.domain.queue.repositoties.QueueRepository;
import org.springframework.stereotype.Repository;

@Repository
public class QueueRepositoryImpl implements QueueRepository {
    private final JpaQueueRepository jpaQueueRepository;

    public QueueRepositoryImpl(JpaQueueRepository jpaQueueRepository) {
        this.jpaQueueRepository = jpaQueueRepository;
    }

    @Override
    public int getQueuePositionInWaitingList(Long queueId, QueueStatus queueStatus) {
        return jpaQueueRepository.getQueuePositionInWaitingList(queueId, queueStatus);
    }

    @Override
    public Queue findByUserIdAndStatus(Long userId, QueueStatus queueStatus) {
        return jpaQueueRepository.findByUserIdAndStatus(userId, queueStatus);
    }

    @Override
    public Queue findByToken(String token) {
        return jpaQueueRepository.findByToken(token);
    }

    @Override
    public void save(Queue queue) {
        jpaQueueRepository.save(queue);
    }
}
