package hhplus.concert.application.unit.queue.usecase;

import hhplus.concert.application.queue.dto.QueueServiceDto;
import hhplus.concert.application.queue.usecase.QueueFacade;
import hhplus.concert.domain.queue.components.QueueService;
import hhplus.concert.domain.queue.models.Queue;
import hhplus.concert.domain.user.components.UserService;
import hhplus.concert.domain.user.models.User;
import hhplus.concert.support.type.QueueStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

class QueueFacadeTest {
    @Mock
    private UserService userService;

    @Mock
    private QueueService queueService;

    @InjectMocks
    private QueueFacade queueFacade;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private static final String TOKEN = "test_token";

    @Test
    void 대기열이_있는경우에_토큰발급요청시_이전_대기열을_만료로하고_새롭게_대기열과_토큰을_발급() {
        // Given
        Long userId = 1L;
        User mockUser = new User("scope");
        Queue mockQueue = Queue.builder()
                .token(TOKEN)
                .user(mockUser)
                .status(QueueStatus.WAIT)
                .build();

        when(userService.findUserInfo(userId)).thenReturn(mockUser);
        when(queueService.findByUserIdAndStatus(userId, QueueStatus.WAIT)).thenReturn(mockQueue);
        when(queueService.enqueueAndGenerateToken(mockUser)).thenReturn(TOKEN);

        // When
        QueueServiceDto.IssuedToken issuedToken = queueFacade.issueQueueToken(userId);
        // Then
        assertNotNull(issuedToken);
        assertThat(TOKEN).isEqualTo(issuedToken.token());
        verify(queueService).updateStatus(mockQueue, QueueStatus.EXPIRED); // 이전 큐를 만료 상태로 업데이트
        verify(queueService).enqueueAndGenerateToken(mockUser); // 새롭게 대기열에 추가 후 토큰 발급
    }


    @Test
    void 대기열이_없는경우에_토큰발급요청시_새롭게_대기열과_토큰을_발급() {
        // Given
        Long userId = 1L;
        User mockUser = new User("scope");

        when(userService.findUserInfo(userId)).thenReturn(mockUser);
        when(queueService.findByUserIdAndStatus(userId, QueueStatus.WAIT)).thenReturn(null);
        when(queueService.enqueueAndGenerateToken(mockUser)).thenReturn(TOKEN);

        // When
        QueueServiceDto.IssuedToken issuedToken = queueFacade.issueQueueToken(userId);

        // Then
        assertNotNull(issuedToken);
        assertThat(TOKEN).isEqualTo(issuedToken.token());
        verify(queueService, never()).updateStatus(any(Queue.class), any(QueueStatus.class)); // 기존 큐가 없으므로 업데이트하지 않음
        verify(queueService).enqueueAndGenerateToken(mockUser);
    }

    @Test
    void 토큰을_통해_대기열_정보_조회하면_상태와_현재_대기열_순번도_같이_리턴() {
        // Given
        User mockUser = new User("scope");
        Queue mockQueue = Queue.builder()
                .id(1L)
                .token(TOKEN)
                .user(mockUser)
                .status(QueueStatus.WAIT)
                .build();

        when(queueService.findQueueByToken(TOKEN)).thenReturn(mockQueue);
        when(queueService.getQueuePositionInWaitingList(1L, QueueStatus.WAIT)).thenReturn(5);

        // When
        QueueServiceDto.Queue queueInfo = queueFacade.findQueueByToken(TOKEN);

        // Then
        assertNotNull(queueInfo);
        assertThat(QueueStatus.WAIT).isEqualTo(queueInfo.status());
        assertThat(5).isEqualTo(queueInfo.queuePosition());
    }


    @Test
    void 활성화_시킬_대기열_목록을_확인후_최대_허용_개수보다_적은경우_상태를_변경() {
        // Given
        when(queueService.getQueueCountByStatus(QueueStatus.ACTIVE)).thenReturn(97);
        when(queueService.getActivatedIdsFromWaitingQueues(3)).thenReturn(List.of(1L, 2L, 3L));

        // When
        queueFacade.maintainActiveQueueCountWithScheduler();

        // Then
        verify(queueService).updateQueuesToActive(List.of(1L, 2L, 3L), QueueStatus.ACTIVE);
    }

    @Test
    void 활성화_시킬_대기열_목록을_확인후_최대_허용_개수보다_많은경우_상태를_변경하지않음() {
        // Given
        when(queueService.getQueueCountByStatus(QueueStatus.ACTIVE)).thenReturn(100);
        when(queueService.getActivatedIdsFromWaitingQueues(0)).thenReturn(Collections.emptyList()); // 대기열이 없음

        // When
        queueFacade.maintainActiveQueueCountWithScheduler();

        // Then
        verify(queueService, never()).updateQueuesToActive(anyList(), any(QueueStatus.class));
    }
}