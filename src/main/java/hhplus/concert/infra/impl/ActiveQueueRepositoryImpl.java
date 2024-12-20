package hhplus.concert.infra.impl;

import hhplus.concert.domain.queue.repositoties.ActiveQueueRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ActiveQueueRepositoryImpl implements ActiveQueueRepository {

    private final RedisTemplate<String, String> redisTemplate;
    public static final String WAITING_QUEUE_KEY = "waitingQueue";
    public static final String ACTIVE_QUEUE_KEY = "activeQueue";


    public ActiveQueueRepositoryImpl(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
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
