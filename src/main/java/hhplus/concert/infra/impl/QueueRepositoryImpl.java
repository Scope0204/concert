package hhplus.concert.infra.impl;

import hhplus.concert.domain.queue.models.Queue;
import hhplus.concert.domain.queue.repositoties.QueueRepository;
import hhplus.concert.infra.jpa.JpaQueueRepository;
import hhplus.concert.support.error.ErrorCode;
import hhplus.concert.support.error.exception.BusinessException;
import hhplus.concert.support.type.QueueStatus;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class QueueRepositoryImpl implements QueueRepository {
    private final JpaQueueRepository jpaQueueRepository;

    public QueueRepositoryImpl(JpaQueueRepository jpaQueueRepository) {
        this.jpaQueueRepository = jpaQueueRepository;
    }

    @Override
    public void updateQueuesToActive(List<Long> queueIds, QueueStatus queueStatus) {
        jpaQueueRepository.updateQueuesToActive(queueIds, queueStatus);
    }

    @Override
    public List<Queue> findAll() {
        return jpaQueueRepository.findAll();
    }

    @Override
    public List<Queue> findTopByStatusOrderByIdAsc(QueueStatus queueStatus, int limit) {
        return jpaQueueRepository.findTopByStatusOrderByIdAsc(queueStatus, limit);
    }

    @Override
    public int getQueueCountByStatus(QueueStatus queueStatus) {
        return jpaQueueRepository.getQueueCountByStatus(queueStatus);
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
        return jpaQueueRepository.findByToken(token)
                .orElseThrow(() -> new BusinessException(ErrorCode.QUEUE_NOT_FOUND));
    }

    @Override
    public void save(Queue queue) {
        jpaQueueRepository.save(queue);
    }
}
