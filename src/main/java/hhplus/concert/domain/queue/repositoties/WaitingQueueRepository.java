package hhplus.concert.domain.queue.repositoties;

import java.util.Set;

public interface WaitingQueueRepository {
    // WaitingQueue
    void addToWaitingQueue(String token, Double position);
    boolean findWaitingQueueByToken(String token);
    Long getPositionInWaitingList(String token);
    Set<String> getWaitingQueueTokensFromActiveQueues(Long needToUpdateCount);
}
