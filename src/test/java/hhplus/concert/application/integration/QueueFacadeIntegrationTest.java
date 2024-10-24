package hhplus.concert.application.integration;

import hhplus.concert.application.queue.dto.QueueServiceDto;
import hhplus.concert.application.queue.usecase.QueueFacade;
import hhplus.concert.domain.queue.models.Queue;
import hhplus.concert.domain.queue.repositoties.QueueRepository;
import hhplus.concert.domain.user.models.User;
import hhplus.concert.domain.user.repositories.UserRepository;
import hhplus.concert.support.error.ErrorCode;
import hhplus.concert.support.error.exception.BusinessException;
import hhplus.concert.support.type.QueueStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class QueueFacadeIntegrationTest {

    @Autowired
    private QueueFacade queueFacade;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private QueueRepository queueRepository;

    @Nested
    @DisplayName("[issueQueueToken] 대기열 토큰 발급 테스트")
    class issueQueueTokenTests {

        @Test
        void 새로운_대기열과_토큰을_생성(){
            // given
            Long userId = 1L;

            // when
            QueueServiceDto.IssuedToken result = queueFacade.issueQueueToken(userId);

            // then
            Queue queue = queueRepository.findByUserIdAndStatus(userId, QueueStatus.WAIT); // 대기중인 대기열 조회

            assertNotNull(result.token());
            assertNotNull(queue);
            assertEquals(result.token(), queue.getToken());
        }

        @Test
        void 유효한_사용자가_토큰을_가지고_있는경우_새로운_토큰을_생성(){
            // given
            Long userId = 1L;

            // when
            QueueServiceDto.IssuedToken result1 = queueFacade.issueQueueToken(userId);
            System.out.println(result1.token());
            QueueServiceDto.IssuedToken result2 = queueFacade.issueQueueToken(userId);
            System.out.println(result2.token()); // TODO: 트랜잭션 처리나 테스트 환경의 초기화 문제? 토큰이 새롭게 발급 되지 않는 문제가 발생

            // then
            Queue queue1 = queueRepository.findByToken(result1.token());
            Queue queue2 = queueRepository.findByToken(result2.token());

            assertNotNull(queue1);
            assertNotNull(queue2);
            assertEquals(queue1.getUser().getId(), queue2.getUser().getId(), userId);
            assertEquals(queue1.getStatus(), QueueStatus.EXPIRED);
            assertEquals(queue2.getStatus(), QueueStatus.WAIT);
        }

        @Test
        void 사용자가_존재하지않는경우_예외_발생 () {
            // given
            Long userId = -999L;

            // When & Then
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                queueFacade.issueQueueToken(userId);
            });
            assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
        }

        @Test
        void 다른_사용자가_대기열에들어와도_대기_상태는_유지되어야한다 () {
            // given
            Long userId1 = 1L;
            Long userId2 = 2L;

            // when
            QueueServiceDto.IssuedToken result1 = queueFacade.issueQueueToken(userId1);
            QueueServiceDto.IssuedToken result2 = queueFacade.issueQueueToken(userId2);

            // then
            Queue queue1 = queueRepository.findByUserIdAndStatus(userId1, QueueStatus.WAIT);
            assertNotNull(queue1);
            assertNotNull(result1.token());
            assertEquals(result1.token(), queue1.getToken());

            Queue queue2 = queueRepository.findByUserIdAndStatus(userId2, QueueStatus.WAIT);
            assertNotNull(queue2);
            assertNotNull(result2.token());
            assertEquals(result2.token(), queue2.getToken());
        }

    }

    @Nested
    @DisplayName("[findQueueByToken] 토큰을 통해 대기열 정보 조회 테스트")
    class findQueueByTokenTests {

        @Test
        void 유저_토큰을_통해_대기중인_대기열_정보_조회() {
            // given
            List<User> Users = userRepository.findByAll();
            for (User user : Users) {
                String token = UUID.randomUUID().toString();
                Queue queue = Queue.builder()
                        .user(user)
                        .token(token)
                        .status(QueueStatus.WAIT)
                        .createdAt(LocalDateTime.now())
                        .build();
                queueRepository.save(queue);
            }

            User testUser = new User("scope");
            userRepository.save(testUser);
            String testToken = "testToken";
            Queue queue = Queue.builder()
                    .user(testUser)
                    .token(testToken)
                    .status(QueueStatus.WAIT)
                    .createdAt(LocalDateTime.now())
                    .build();
            queueRepository.save(queue);

            // when
            QueueServiceDto.Queue result = queueFacade.findQueueByToken(testToken);

            // then
            List<Queue> queues = queueRepository.findAll();
            assertEquals(4, queues.size());
            assertEquals(queue.getCreatedAt(), result.createdAt());
            assertEquals(QueueStatus.WAIT, result.status());
            assertEquals(3, result.queuePosition()); // 총 대기열 4명일때, testUser 앞에 3명이 존재
        }

        @Test
        void 대기중이_아닌_대기열은_queuePosition값이_0_이어야_한다() {
            User testUser = new User("scope");
            userRepository.save(testUser);

            String testToken = "testToken";
            Queue queue = Queue.builder()
                    .user(testUser)
                    .token(testToken)
                    .status(QueueStatus.ACTIVE)
                    .createdAt(LocalDateTime.now())
                    .enteredAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            queueRepository.save(queue);

            // when
            QueueServiceDto.Queue result = queueFacade.findQueueByToken(testToken);

            // then
            assertEquals(queue.getCreatedAt(), result.createdAt());
            assertEquals(QueueStatus.ACTIVE, result.status());
            assertEquals(0, result.queuePosition());

        }

        @Test
        void 유효하지_않는_토큰으로_조회하는_경우_에러반환() {
            String invalidToken = "invalid-token";
            // when & then
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                queueFacade.findQueueByToken(invalidToken);
            });
            assertEquals(ErrorCode.QUEUE_NOT_FOUND, exception.getErrorCode());
        }
    }
}
