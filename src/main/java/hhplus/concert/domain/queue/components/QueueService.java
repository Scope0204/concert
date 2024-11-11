package hhplus.concert.domain.queue.components;

import hhplus.concert.domain.queue.repositoties.ActiveQueueRepository;
import hhplus.concert.domain.queue.repositoties.WaitingQueueRepository;
import hhplus.concert.support.type.QueueStatus;
import hhplus.concert.support.util.JwtUtil;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class QueueService {
    private final JwtUtil jwtUtil;
    private final WaitingQueueRepository waitingQueueRepository;
    private final ActiveQueueRepository activeQueueRepository;
    private static final int ACTIVE_MAX_SIZE = 100;
    public static final long TOKEN_EXPIRATION_TIME = 10L * 60L * 1000; // 10 minutes

    public QueueService(JwtUtil jwtUtil, WaitingQueueRepository waitingQueueRepository, ActiveQueueRepository activeQueueRepository) {
        this.jwtUtil = jwtUtil;
        this.waitingQueueRepository = waitingQueueRepository;
        this.activeQueueRepository = activeQueueRepository;
    }

    // userId를 통해 JWT 토큰을 발급하고, WaitingQueue 에 추가한다.
    public String enqueueAndGenerateToken(Long userId) {
        String token = jwtUtil.generateToken(userId);
        double score = (double) System.currentTimeMillis();

        waitingQueueRepository.addToWaitingQueue(token, score);
        return token;
    }

    // 해당 토큰의 상태를 조회 (어느 대기열에 존재하는지 유무에 따라 달라짐)
    public QueueStatus getQueueStatus(String token) {
        if (activeQueueRepository.findActiveQueueByToken(token)) {
            return QueueStatus.ACTIVE;
        } else if (waitingQueueRepository.findWaitingQueueByToken(token)) {
            return QueueStatus.WAIT;
        } else {
            return QueueStatus.EXPIRED;
        }
    }

    public Long getQueuePositionInWaitingList(String token) {
        return waitingQueueRepository.getPositionInWaitingList(token);
    }

    // ActiveQueue 에 수용가능한 Token 개수를 구하여, 해당 개수만큼 WaitingQueue Token 을 ActiveQueue 로 추가한다.(WaitingQueue 는 삭제)
    public void updateToActiveTokens() {
        Long getActiveQueueCount = activeQueueRepository.getActiveQueueCount();
        Long needToUpdateCount = Math.max(ACTIVE_MAX_SIZE - getActiveQueueCount, 0L);
        if (needToUpdateCount == 0L) {
            return;
        }
        Set<String> tokensNeedToUpdate = waitingQueueRepository.getWaitingQueueTokensFromActiveQueues(needToUpdateCount);

        for (String token : tokensNeedToUpdate) {
            activeQueueRepository.updateToActiveQueue(
                    token,
                    System.currentTimeMillis() + TOKEN_EXPIRATION_TIME
            );
        }
    }

    public void removeExpiredActiveQueue() {
        activeQueueRepository.removeExpiredActiveQueue(System.currentTimeMillis());
    }

    public void removeCompletedActiveQueue(String token) {
        activeQueueRepository.removeCompletedActiveQueue(token);
    }

}
