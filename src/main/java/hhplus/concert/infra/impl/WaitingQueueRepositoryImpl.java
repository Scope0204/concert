package hhplus.concert.infra.impl;

import hhplus.concert.domain.queue.repositoties.WaitingQueueRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.Set;

@Repository
public class WaitingQueueRepositoryImpl implements WaitingQueueRepository {

    private final RedisTemplate<String, String> redisTemplate;
    public static final String WAITING_QUEUE_KEY = "waitingQueue";

    public WaitingQueueRepositoryImpl(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void addToWaitingQueue(String token,  Double score) {
        redisTemplate.opsForZSet().add(WAITING_QUEUE_KEY, token, score);
    }

    @Override
    public boolean findWaitingQueueByToken(String token) {
        return redisTemplate.opsForZSet().score(WAITING_QUEUE_KEY, token) != null;
    }

    /**
     *  Token 이 WaitingQueue 에서 몇번째인지 순서를 리턴
     */
    @Override
    public Long getPositionInWaitingList(String token) {
        Long rank = redisTemplate.opsForZSet().rank(WAITING_QUEUE_KEY, token);
        return (rank != null) ? rank : -1;
    }

    /**
     * WaitingQueue 중 ActiveQueue 로 변경 할 수 있는 수만큼의 WaitingQueue Token 을 가지고 옴
     */
    @Override
    public Set<String> getWaitingQueueTokensFromActiveQueues(Long needToUpdateCount) {
        Set<String> tokensNeedToUpdate = redisTemplate.opsForZSet().range(WAITING_QUEUE_KEY, 0, needToUpdateCount - 1);
        return (tokensNeedToUpdate != null) ? tokensNeedToUpdate : Collections.emptySet();
    }
}
