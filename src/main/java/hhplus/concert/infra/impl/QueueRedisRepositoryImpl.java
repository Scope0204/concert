package hhplus.concert.infra.impl;

import hhplus.concert.domain.queue.repositoties.QueueRedisRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.Set;

@Repository
public class QueueRedisRepositoryImpl implements QueueRedisRepository {

    private final RedisTemplate<String, String> redisTemplate;
    public static final String WAITING_QUEUE_KEY = "waitingQueue";
    public static final String ACTIVE_QUEUE_KEY = "activeQueue";


    public QueueRedisRepositoryImpl(RedisTemplate<String, String> redisTemplate) {
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

    /**
     * Token 정보를 통해 ActiveQueue 조회
     */
    @Override
    public boolean findActiveQueueByToken(String token) {
        return redisTemplate.opsForZSet().score(ACTIVE_QUEUE_KEY, token) != null;
    }

    /**
     * WAIT 상태의 현재 대기열을 삭제하고, ACTIVE 상태를 등록한다.
     */
    @Override
    public void updateToActiveQueue(String token, Long expireTime) {
        redisTemplate.opsForZSet().remove(WAITING_QUEUE_KEY, token);
        redisTemplate.opsForZSet().add(ACTIVE_QUEUE_KEY, token, expireTime.doubleValue());
    }

    /**
     * 현재 ACTIVE 상태의 대기열이 총 몇개인지 확인한다.
     */
    @Override
    public Long getActiveQueueCount() {
        return redisTemplate.opsForZSet().size(ACTIVE_QUEUE_KEY);
    }

    /**
     *  현재 ACTIVE 상태의 대기열 중, 만료된 대기열을 삭제 (expireTime 이 currentTime 보다 이전인 데이터)
     */
    @Override
    public void removeExpiredActiveQueue(Long currentTime) {
        redisTemplate.opsForZSet().removeRangeByScore(ACTIVE_QUEUE_KEY, Double.NEGATIVE_INFINITY, (double) currentTime);
    }

    /**
     *  결제 까지 완료된 대기열 토큰을 삭제. 토큰이 일치하는 값을 삭제.
     */
    @Override
    public void removeCompletedActiveQueue(String token) {
        redisTemplate.opsForZSet().remove(ACTIVE_QUEUE_KEY,token);
    }
}
