package hhplus.concert.domain.queue.components;

import hhplus.concert.domain.queue.models.Queue;
import hhplus.concert.domain.queue.repositoties.QueueRepository;
import hhplus.concert.domain.user.models.User;
import hhplus.concert.support.type.QueueStatus;
import hhplus.concert.support.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

class QueueServiceTest {

    @Mock
    private QueueRepository queueRepository;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private QueueService queueService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // 상수 정의
    private static final String TOKEN = "test_token";

    /*
    존재하지않는 유저로 대기열을 조회하면 null을 리턴
    존재하는 유저와 상태로 대기열을 조히하면 올바른 대기열 리턴
    존재하는 유저로 대기열을 생성하고 토큰을 생성하여 리턴
    대기열의 상태를 정상적으로 업데이트(wait->apply)
*/
    @Test
    void 존재하는_유저와_대기열상태로_대기열을_조회하면_올바른_대기열_리턴(){
        // Given
        User user = new User("jkcho");
        Long userId = user.getId();
        Queue queue = Queue.builder()
                .user(user)
                .token(TOKEN)
                .status(QueueStatus.WAIT)
                .createdAt(LocalDateTime.now())
                .build();

        // When
        when(queueRepository.findByUserIdAndStatus(userId,QueueStatus.WAIT)).thenReturn(queue);
        Queue result = queueService.findByUserIdAndStatus(userId,QueueStatus.WAIT);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(userId);
        assertThat(result.getToken()).isEqualTo(TOKEN);
        assertThat(result.getStatus()).isEqualTo(QueueStatus.WAIT);
    }

    @Test
    void 대기열의_상태를_정상적으로_업데이트() {
        // Given
        User user = new User("jkcho");
        Queue queue = Queue.builder()
                .user(user)
                .token(TOKEN)
                .status(QueueStatus.WAIT)
                .createdAt(LocalDateTime.now())
                .build();

        // When
        queueService.updateStatus(queue,QueueStatus.ACTIVE);

        // Then
        assertThat(queue.getStatus()).isEqualTo(QueueStatus.ACTIVE);
        verify(queueRepository).save(queue);
    }

    @Test
    void 대기열을_추가하고_토큰을_발급받아_리턴() {
        // Given
        User user = new User("jkcho");
        when(jwtUtil.generateToken(user.getId())).thenReturn(TOKEN);

        // When
        String result = queueService.enqueueAndGenerateToken(user);

        // Then
        assertThat(result).isEqualTo(TOKEN);
        verify(jwtUtil).generateToken(user.getId());
    }

    @Test
    void 토큰을_통해_대기열_조회() {
        // Given
        User user = new User("jkcho");
        Queue queue = Queue.builder()
                .user(user)
                .token(TOKEN)
                .status(QueueStatus.WAIT)
                .createdAt(LocalDateTime.now())
                .build();

        when(queueRepository.findByToken(TOKEN)).thenReturn(queue);

        // When
        Queue result = queueService.findQueueByToken(TOKEN);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(queue);
    }

    @Test
    void 대기열상태가_WAIT_상태일때_순번조회 () {
        // Given
        Long queueId = 1L;
        QueueStatus status = QueueStatus.WAIT;
        when(queueRepository.getQueuePositionInWaitingList(queueId, status)).thenReturn(3);

        // When
        int position = queueService.getQueuePositionInWaitingList(queueId, status);

        // Then
        assertThat(position).isEqualTo(3);
        verify(queueRepository, times(1)).getQueuePositionInWaitingList(queueId, status);
    }

    @Test
    void 대기열상태가_WAIT_상태가_아닐때_순번조회() {
        // Given
        Long queueId = 1L;
        QueueStatus status = QueueStatus.ACTIVE;

        // When
        int position = queueService.getQueuePositionInWaitingList(queueId, status);

        // Then
        assertThat(position).isEqualTo(0);
        verify(queueRepository, never()).getQueuePositionInWaitingList(anyLong(), any());
    }

    @Test
    void 대기열_상태를_확인하여_대기순번을_리턴() {
        // Given
        QueueStatus status = QueueStatus.WAIT;
        when(queueRepository.getQueueCountByStatus(status)).thenReturn(10);

        // When
        int count = queueService.getQueueCountByStatus(status);

        // Then
        assertThat(count).isEqualTo(10);
        verify(queueRepository, times(1)).getQueueCountByStatus(status);
    }

    @Test
    void 활성화_상태로_변경시킬_대기중인_대기열의_ID_리스트를_순서대로_리턴 () {
        // Given
        int needToUpdateCount = 5;
        Queue queue1 = Queue.builder()
                .id(1L)
                .user(new User("jkcho"))
                .token(TOKEN)
                .status(QueueStatus.WAIT)
                .createdAt(LocalDateTime.now())
                .build();
        Queue queue2 = Queue.builder()
                .id(2L)
                .user(new User("jkcho2"))
                .token(TOKEN)
                .status(QueueStatus.WAIT)
                .createdAt(LocalDateTime.now())
                .build();
        Queue queue3 = Queue.builder()
                .id(3L)
                .user(new User("jkcho3"))
                .token(TOKEN)
                .status(QueueStatus.WAIT)
                .createdAt(LocalDateTime.now())
                .build();
        List<Queue> queues = List.of(
                queue1,
                queue2,
                queue3
        );
        when(queueRepository.findTopByStatusOrderByIdAsc(QueueStatus.WAIT, needToUpdateCount)).thenReturn(queues);

        // When
        List<Long> activatedIds = queueService.getActivatedIdsFromWaitingQueues(needToUpdateCount);
        System.out.println(activatedIds);
        // Then
        assertThat(activatedIds).containsExactly(1L,2L,3L);
        verify(queueRepository, times(1)).findTopByStatusOrderByIdAsc(QueueStatus.WAIT, needToUpdateCount);
    }

    @Test
    void 대기중인_상태의_대기열을_활성화_상태로_변경() {
        // Given
        List<Long> queueIds = List.of(1L, 2L, 3L);
        QueueStatus status = QueueStatus.ACTIVE;

        // When
        queueService.updateQueuesToActive(queueIds, status);

        // Then
        verify(queueRepository, times(1)).updateQueuesToActive(queueIds, status);
    }
}