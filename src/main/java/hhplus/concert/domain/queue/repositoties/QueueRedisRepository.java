package hhplus.concert.domain.queue.repositoties;

import java.util.Set;

public interface QueueRedisRepository {
    // WaitingQueue
    void addToWaitingQueue(String token, Double position);
    boolean findWaitingQueueByToken(String token);
    Long getPositionInWaitingList(String token);
    Set<String> getWaitingQueueTokensFromActiveQueues(Long needToUpdateCount);
    // ActiveQueue
    boolean findActiveQueueByToken(String token);
    void updateToActiveQueue(String token, Long expireTime);
    Long getActiveQueueCount();
    // delete
    void removeExpiredActiveQueue(Long currentTime);
    void removeCompletedActiveQueue(String token);
}
