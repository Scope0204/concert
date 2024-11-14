package hhplus.concert.application.queue.usecase;

import hhplus.concert.application.queue.dto.QueueServiceDto;
import hhplus.concert.domain.queue.components.QueueService;
import hhplus.concert.domain.user.components.UserService;
import hhplus.concert.domain.user.models.User;
import hhplus.concert.support.type.QueueStatus;
import org.springframework.stereotype.Service;

@Service
public class QueueFacade {

    private final UserService userService;
    private final QueueService queueService;

    public QueueFacade(UserService userService, QueueService queueService) {
        this.userService = userService;
        this.queueService = queueService;
    }

    /**
     * 1. 대기열 토큰 발급
     * userId 유저가 존재하는지 검증
     * 토큰을 발급 후 WaitingQueue Redis SortedSet 에 저장. 토큰 정보를 전달
     */
    public QueueServiceDto.IssuedToken issueQueueToken(Long userId){
        User user = userService.findUserInfo(userId);
        String queueToken = queueService.enqueueAndGenerateToken(user.getId());
        return new QueueServiceDto.IssuedToken(queueToken);
    }

    /**
     * 2. 유저 토큰을 통해 대기열 정보 조회(polling 용 api)
     * header Token 을 통해 토큰 상태를 반환한다.(어느 대기열에 위치하느냐에 따라 다르다)
     * 현재 대기열에서, 순서가 얼마나 남았는지를 같이 반환한다.
     */
    public QueueServiceDto.Queue findQueueByToken(String token){

        QueueStatus queueStatus = queueService.getQueueStatus(token);
        Long queuePosition = (queueStatus == QueueStatus.WAIT) ? queueService.getQueuePositionInWaitingList(token) : 0;

        return new QueueServiceDto.Queue(
                queueStatus,
                queuePosition
        );
    }

    /**
     * 스케줄러를 통해 WaitingQueue 상태를 관리.
     * 순서대로 토큰을 ActiveQueue 로 업데이트
     */
    public void updateToActiveTokens() {
        queueService.updateToActiveTokens();
    }

    /**
     * 스케쥴러를 통해 만료된 ActiveQueue 삭제
     */
    public void cancelExpiredActiveQueue() {
        queueService.removeExpiredActiveQueue();
    }
}
