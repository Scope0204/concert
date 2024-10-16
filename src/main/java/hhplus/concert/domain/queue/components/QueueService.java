package hhplus.concert.domain.queue.components;

import hhplus.concert.support.type.QueueStatus;
import hhplus.concert.support.util.JwtUtil;
import hhplus.concert.domain.queue.models.Queue;
import hhplus.concert.domain.queue.repositoties.QueueRepository;
import hhplus.concert.domain.user.models.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class QueueService {
    private final JwtUtil jwtUtil;
    private final QueueRepository queueRepository;

    public QueueService(JwtUtil jwtUtil, QueueRepository queueRepository) {
        this.jwtUtil = jwtUtil;
        this.queueRepository = queueRepository;
    }

    // UserId를 통해 대기열에서 대기중인 정보를 찾는다
    public Queue findByUserIdAndStatus(Long userId, QueueStatus queueStatus){
        return queueRepository.findByUserIdAndStatus(userId, queueStatus);
    }

    // 대기중인 대기열 상태를 만료로 변경한다
    @Transactional
    public void updateStatus(Queue queue, QueueStatus queueStatus){
        queue.updateStatus(queueStatus);
        queueRepository.save(queue);
    }

    // 대기열에 추가하고 토큰을 발급한다
    @Transactional
    public String enqueueAndGenerateToken(User user) {
        String token = jwtUtil.generateToken(user.getId());

        Queue queue = Queue.builder()
                .user(user)
                .token(token)
                .status(QueueStatus.WAIT)
                .createdAt(LocalDateTime.now())
                .build();
        queueRepository.save(queue);
        return token;
    }

    // 토큰 정보를 통해 대기열 조회
    public Queue findQueueByToken(String token) {
        return queueRepository.findByToken(token);
    }

    // 대기열 상태가 WAIT 인 상태 중 해당 대기열이 몇번째에 있는지 확인.
    // 다른 상태의 경우 대기열의 의미가 없으므로 0을 반환
    public int getQueuePositionInWaitingList(Long queueId, QueueStatus status) {
        return (status == QueueStatus.WAIT)
                ? queueRepository.getQueuePositionInWaitingList(queueId, status)
                : 0;
    }
}
