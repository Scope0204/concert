package hhplus.concert.domain.queue.components;

import hhplus.concert.domain.queue.models.Queue;
import hhplus.concert.domain.queue.repositoties.QueueRepository;
import hhplus.concert.domain.user.models.User;
import hhplus.concert.support.type.QueueStatus;
import hhplus.concert.support.util.JwtUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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

    // 대기열의 상태를 변경한다
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

    // 대기열 상태가 WAIT 인 상태 중 해당 대기열이 몇번째에 있는지 확인
    // 다른 상태의 경우 대기열의 의미가 없으므로 0을 반환
    public int getQueuePositionInWaitingList(Long queueId, QueueStatus queueStatus) {
        return (queueStatus == QueueStatus.WAIT)
                ? queueRepository.getQueuePositionInWaitingList(queueId, queueStatus)
                : 0;
    }

    // 대기열 상태를 확인
    public int getQueueCountByStatus(QueueStatus queueStatus){
        return queueRepository.getQueueCountByStatus(queueStatus);
    }

    // 대기중인 상태(WAIT)의 대기열을 순서대로 활성화(ACTIVE) 상태로 변경시킬, 해당 대기열의 ID 리스트를 반환한다
    public List<Long> getActivatedIdsFromWaitingQueues(int needToUpdateCount){
        return queueRepository
                .findTopByStatusOrderByIdAsc(
                        QueueStatus.WAIT,
                        needToUpdateCount
                ).stream()
                .map(queue -> queue.getId())
                .collect(Collectors.toList());
    }

    // 대기중인 상태(WAIT)의 대기열을 활성화(ACTIVE) 상태로 변경
    @Transactional
    public void updateQueuesToActive(List<Long> queueIds, QueueStatus queueStatus){
        queueRepository.updateQueuesToActive(queueIds, queueStatus);
    }
}
