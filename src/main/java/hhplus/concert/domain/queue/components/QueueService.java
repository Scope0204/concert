package hhplus.concert.domain.queue.components;

import hhplus.concert.domain.queue.repositoties.QueueRedisRepository;
import hhplus.concert.support.type.QueueStatus;
import hhplus.concert.support.util.JwtUtil;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class QueueService {
    private final JwtUtil jwtUtil;
    private final QueueRedisRepository queueRedisRepository;
    private static final int ACTIVE_MAX_SIZE = 100;
    public static final long TOKEN_EXPIRATION_TIME = 10L * 60L * 1000; // 10 minutes

    public QueueService(JwtUtil jwtUtil, QueueRedisRepository queueRedisRepository) {
        this.jwtUtil = jwtUtil;
        this.queueRedisRepository = queueRedisRepository;
    }

    // userId를 통해 JWT 토큰을 발급하고, WaitingQueue 에 추가한다.
    public String enqueueAndGenerateToken(Long userId) {
        String token = jwtUtil.generateToken(userId);
        double score = (double) System.currentTimeMillis();

        queueRedisRepository.addToWaitingQueue(token, score);
        return token;
    }

    // 해당 토큰의 상태를 조회 (어느 대기열에 존재하는지 유무에 따라 달라짐)
    public QueueStatus getQueueStatus(String token) {
        if (queueRedisRepository.findActiveQueueByToken(token)) {
            return QueueStatus.ACTIVE;
        } else if (queueRedisRepository.findWaitingQueueByToken(token)) {
            return QueueStatus.WAIT;
        } else {
            return QueueStatus.EXPIRED;
        }
    }

    public Long getQueuePositionInWaitingList(String token) {
        return queueRedisRepository.getPositionInWaitingList(token);
    }

    // ActiveQueue 에 수용가능한 Token 개수를 구하여, 해당 개수만큼 WaitingQueue Token 을 ActiveQueue 로 추가한다.(WaitingQueue 는 삭제)
    public void updateToActiveTokens() {
        Long getActiveQueueCount = queueRedisRepository.getActiveQueueCount();
        Long needToUpdateCount = Math.max(ACTIVE_MAX_SIZE - getActiveQueueCount, 0L);
        if (needToUpdateCount == 0L) {
            return;
        }
        Set<String> tokensNeedToUpdate = queueRedisRepository.getWaitingQueueTokensFromActiveQueues(needToUpdateCount);

        for (String token : tokensNeedToUpdate) {
            queueRedisRepository.updateToActiveQueue(
                    token,
                    System.currentTimeMillis() + TOKEN_EXPIRATION_TIME
            );
        }
    }

    public void removeExpiredActiveQueue() {
        queueRedisRepository.removeExpiredActiveQueue(System.currentTimeMillis());
    }

    public void removeCompletedActiveQueue(String token) {
        queueRedisRepository.removeCompletedActiveQueue(token);
    }

}
