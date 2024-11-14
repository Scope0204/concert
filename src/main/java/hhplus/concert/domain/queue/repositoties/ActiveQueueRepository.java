package hhplus.concert.domain.queue.repositoties;

public interface ActiveQueueRepository {
    // ActiveQueue
    boolean findActiveQueueByToken(String token);
    void updateToActiveQueue(String token, Long expireTime);
    Long getActiveQueueCount();
    void removeExpiredActiveQueue(Long currentTime);
    void removeCompletedActiveQueue(String token);
}
