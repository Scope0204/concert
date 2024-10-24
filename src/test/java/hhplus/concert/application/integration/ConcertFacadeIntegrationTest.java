package hhplus.concert.application.integration;

import hhplus.concert.application.concert.dto.ConcertServiceDto;
import hhplus.concert.application.concert.usecase.ConcertFacade;
import hhplus.concert.domain.concert.models.Concert;
import hhplus.concert.domain.concert.models.ConcertSchedule;
import hhplus.concert.domain.concert.models.Seat;
import hhplus.concert.domain.concert.repositories.ConcertRepository;
import hhplus.concert.domain.concert.repositories.ConcertScheduleRepository;
import hhplus.concert.domain.concert.repositories.SeatRepository;
import hhplus.concert.domain.queue.models.Queue;
import hhplus.concert.domain.queue.repositoties.QueueRepository;
import hhplus.concert.domain.user.models.User;
import hhplus.concert.domain.user.repositories.UserRepository;
import hhplus.concert.support.error.ErrorCode;
import hhplus.concert.support.error.exception.BusinessException;
import hhplus.concert.support.type.ConcertStatus;
import hhplus.concert.support.type.QueueStatus;
import hhplus.concert.support.type.SeatStatus;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest
@Transactional
public class ConcertFacadeIntegrationTest {

    @Autowired
    private ConcertFacade concertFacade;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private QueueRepository queueRepository;

    @Autowired
    private ConcertRepository concertRepository;

    @Autowired
    private ConcertScheduleRepository concertScheduleRepository;

    @Autowired
    private SeatRepository seatRepository;

    private User user;
    private String token;

    @BeforeEach
    void setUp() {
        user = new User("Test User");
        userRepository.save(user);

        token = "test_token";
        queueRepository.save(
                Queue.builder()
                        .user(user)
                        .token(token)
                        .createdAt(LocalDateTime.now().minusHours(1))
                        .enteredAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .status(QueueStatus.ACTIVE)
                        .build()
        );
    }

    /**
     * data.sql 에 필요한 Concert, ConcertSchedule, Seat 테이블의 더미데이터를 미리 추가한 상태.
     */
    @Nested
    @DisplayName("토큰, 대기열 검증 테스트")
    class validate{
        @Test
        void 존재하지않는_토큰으로_요청시_에러발생(){
            // given
            String invalidToken = "invalid Token";

            // when & then
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                concertFacade.getAvailableConcerts(invalidToken);
            });
            assertEquals(ErrorCode.QUEUE_NOT_FOUND, exception.getErrorCode());
        }

        @Test
        void 대기열이_활성화되지않은_상태에서_토큰으로_API_요청시_에러발생(){
            // given
            User testUser = new User("Test User2");
            userRepository.save(testUser);

            String testToken = "expired_token";
            queueRepository.save(
                    Queue.builder()
                            .user(testUser)
                            .token(testToken)
                            .createdAt(LocalDateTime.now().minusHours(1))
                            .enteredAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .status(QueueStatus.EXPIRED)
                            .build()
            );
            // when & then
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                concertFacade.getAvailableConcerts(testToken);
            });
            assertEquals(ErrorCode.QUEUE_NOT_ALLOWED, exception.getErrorCode());
        }
    }

    @Nested
    @DisplayName("[getAvailableConcerts] 콘서트 목록을 조회")
    class getAvailableConcertsTests {
        @Test
        void 콘서트_목록을_조회하여_반환() {
            // when
            List<ConcertServiceDto.Concert> result = concertFacade.getAvailableConcerts(token);

            // then
            // data.sql 에 이용가능한 콘서트를 2개 추가해 둔 상태
            assertEquals(2, result.size());

            List<Concert> concerts = concertRepository.findAll().stream()
                    .filter(concert -> concert.getConcertStatus() == ConcertStatus.AVAILABLE)
                    .collect(Collectors.toList());

            for (int i = 0; i < result.size(); i++) {
                assertEquals(concerts.get(i).getId(), result.get(i).concertId());
                assertEquals(concerts.get(i).getTitle(), result.get(i).title());
                assertEquals(concerts.get(i).getDescription(), result.get(i).description());
            }
        }
    }

    @Nested
    @DisplayName("[getAvailableSchedulesForConcert] 특정 콘서트의 예약 가능한 일정 목록을 조회하여 반환")
    class getAvailableSchedulesForConcert {
        @Test
        void 예약_가능한_콘서트만_스케줄_조회_가능(){
            // given
            Long concertId = 1L;

            // when
            ConcertServiceDto.Schedule result = concertFacade.getAvailableSchedulesForConcert(concertId, token);

            // then
            // data.sql 에 이용가능한 유효한 스케줄을 3개 추가해 둔 상태. 전체 스케줄은 4개 등록된 상태
            List<ConcertSchedule> concertSchedules = concertScheduleRepository.findAllByConcertId(concertId)
                    .stream().filter(concertSchedule -> validateWithinReservationPeriod(concertSchedule.getReservationAt(), concertSchedule.getConcertAt()))
                    .collect(Collectors.toList());

            List<ConcertServiceDto.ConcertSchedule> resultSchedules = result.concertSchedules();

            assertEquals(concertId, result.concertId());
            assertEquals(3, result.concertSchedules().size()); // 예약 시간이 지난 스케줄은 확인 안됨
            for(int i = 0 ; i < concertSchedules.size() ; i++ ){
                assertEquals(concertSchedules.get(i).getId(), resultSchedules.get(i).scheduleId());
                assertEquals(concertSchedules.get(i).getConcertAt(), resultSchedules.get(i).concertAt());
                assertEquals(concertSchedules.get(i).getReservationAt(), resultSchedules.get(i).reservationAt());
            }
        }

        // 예약 가능한 시간대 범위에 있는지 검증하는 로직
        private boolean validateWithinReservationPeriod(LocalDateTime reservationAt, LocalDateTime concertAt) {
            LocalDateTime now = LocalDateTime.now();
            return now.isAfter(reservationAt) && now.isBefore(concertAt);
        }
    }

    @Nested
    @DisplayName("[getAvailableSeats] 콘서트와 날짜 정보를 입력받아 예약가능한 좌석정보를 조회")
    class getAvailableSeatsTests {
        @Test
        void 예약_가능한_좌석_목록을_반환한다(){
            // given
            Long concertId = 1L;
            Long concertScheduleId = 1L;

            // when
            ConcertServiceDto.AvailableSeat result = concertFacade.getAvailableSeats(concertId, concertScheduleId, token);

            // then
            // data.sql 에 이용가능한 좌석을 25개 준비해둔 상태
            List<Seat> availableSeats = seatRepository.findAllByConcertScheduleId(concertScheduleId).stream()
                    .filter(seat -> seat.getStatus() == SeatStatus.AVAILABLE)
                    .collect(Collectors.toList());

            List<ConcertServiceDto.Seat> resultSeats = result.seats();

            assertEquals(concertId, result.concertId());
            assertEquals(25, result.seats().size());

            for( int i = 0 ; i < availableSeats.size() ; i++ ){
                assertEquals(availableSeats.get(i).getId(), resultSeats.get(i).seatId());
                assertEquals(availableSeats.get(i).getSeatNumber(), resultSeats.get(i).seatNumber());
                assertEquals(availableSeats.get(i).getStatus(), resultSeats.get(i).seatStatus());
                assertEquals(availableSeats.get(i).getSeatPrice(), resultSeats.get(i).seatPrice());
            }
        }

        @Test
        void 존재하지_않는_콘서트_ID로_요청시_예외발생(){
            // given
            Long concertId = 999L;
            Long concertScheduleId = 1L;

            // when & then
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                concertFacade.getAvailableSeats(concertId, concertScheduleId, token);
            });
            assertEquals(ErrorCode.CONCERT_NOT_FOUND, exception.getErrorCode());
        }

        @Test
        void 존재하지_않는_스케줄로_요청시_예외발생 (){
            // given
            Long concertId = 1L;
            Long concertScheduleId = 999L;

            // when & then
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                concertFacade.getAvailableSeats(concertId, concertScheduleId, token);
            });
            assertEquals(ErrorCode.CONCERT_SCHEDULE_NOT_FOUND, exception.getErrorCode());
        }

        @Test
        void 예약_불가능한_콘서트에_대해_예외발생 (){
            // given
            // data.sql 에 정의
            Long concertId = 3L;
            Long concertScheduleId = 1L;

            // when & then
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                concertFacade.getAvailableSeats(concertId, concertScheduleId, token);
            });
            assertEquals(ErrorCode.CONCERT_UNAVAILABLE, exception.getErrorCode());
        }

        @Test
        void 예약_가능한_시간이_지난_스케줄로_신청시_예외발생 (){
            // given
            Long concertId = 1L;
            Long concertScheduleId = 4L;

            // when & then
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                concertFacade.getAvailableSeats(concertId, concertScheduleId, token);
            });
            assertEquals(ErrorCode.CONCERT_SCHEDULE_NOT_AVAILABLE, exception.getErrorCode());
        }

    }

}
