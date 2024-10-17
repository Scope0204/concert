package hhplus.concert.application.queue.usecase;

import hhplus.concert.application.queue.dto.QueueServiceDto;
import hhplus.concert.domain.queue.components.QueueService;
import hhplus.concert.domain.queue.models.Queue;
import hhplus.concert.domain.user.components.UserService;
import hhplus.concert.domain.user.models.User;
import hhplus.concert.support.type.QueueStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class QueueFacade {
    @Value("${queue.active-max-size}")
    private int activeMaxSize;

    private final UserService userService;
    private final QueueService queueService;

    public QueueFacade(UserService userService, QueueService queueService) {
        this.userService = userService;
        this.queueService = queueService;
    }

    /**
     * 1. 대기열 토큰 발급
     * userId 검증
     * user 가 존재하는 경우, 대기 중인 대기열 상태를 검증
     * 대기열이 이미 존재하는 경우, 해당 대기열을 만료 상태로 변경
     * 새롭게 대기열 상태에 저장 및 토큰을 발급. 토큰 정보를 전달
     */
    public QueueServiceDto.IssuedToken issueQueueToken(Long userId){
        User user = userService.findUserInfo(userId);
        Queue queue = queueService.findByUserIdAndStatus(userId, QueueStatus.WAIT);
        if(queue!=null){
            queueService.updateStatus(queue, QueueStatus.EXPIRED);
        }
        String queueToken = queueService.enqueueAndGenerateToken(user);

        return new QueueServiceDto.IssuedToken(queueToken);
    }

    /**
     * 2. 유저 토큰을 통해 대기열 정보 조회
     * polling 용 api
     * header Token 을 통해 queue 의 정보를 반환한다.
     * 반환 된 queue 에서 현재 대기열이 얼마나 남았는지를 계산해서 상태를 같이 반환한다.
     */
    public QueueServiceDto.Queue findQueueByToken(String token){
        Queue queue = queueService.findQueueByToken(token);

        // 대기열 순서 계산 후 전달. WAIT 상태 대기열만 고려
        int queuePosition = queueService.getQueuePositionInWaitingList(queue.getId(), queue.getStatus());

        return new QueueServiceDto.Queue(
                queue.getStatus(),
                queue.getCreatedAt(),
                queuePosition
        );
    }

    /**
     * 스케줄러를 통해 대기열 ACTIVE 상태를 관리
     * WAIT 인 queue 중에서 순서대로 ACTIVE 로 업데이트가 가능한 대기열 id 목록을 확인하여
     * ACTIVE 상태로 변경(이때, 시간도 같이 기록)
     */
    public void maintainActiveQueueCountWithScheduler(){
        int needToUpdateCount = activeMaxSize - queueService.getQueueCountByStatus(QueueStatus.ACTIVE);

        if(needToUpdateCount > 0) {
            queueService.updateQueuesToActive(
                    queueService.getActivatedIdsFromWaitingQueues(needToUpdateCount),
                    QueueStatus.ACTIVE);
        }
    }
}
