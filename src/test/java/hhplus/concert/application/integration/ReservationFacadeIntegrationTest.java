package hhplus.concert.application.integration;

import hhplus.concert.application.reservation.dto.ReservationServiceDto;
import hhplus.concert.application.reservation.usecase.ReservationFacade;
import hhplus.concert.domain.concert.repositories.ConcertRepository;
import hhplus.concert.domain.concert.repositories.ConcertScheduleRepository;
import hhplus.concert.domain.concert.repositories.SeatRepository;
import hhplus.concert.domain.queue.models.Queue;
import hhplus.concert.domain.queue.repositoties.QueueRepository;
import hhplus.concert.domain.reservation.models.Reservation;
import hhplus.concert.domain.reservation.repositories.ReservationRepository;
import hhplus.concert.domain.user.models.User;
import hhplus.concert.domain.user.repositories.UserRepository;
import hhplus.concert.support.error.ErrorCode;
import hhplus.concert.support.error.exception.BusinessException;
import hhplus.concert.support.type.QueueStatus;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest
@Transactional
public class ReservationFacadeIntegrationTest {
    @Autowired
    private ReservationFacade reservationFacade;

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

    @Autowired
    private ReservationRepository reservationRepository;

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

    @Test
    void 존재하지않는_토큰으로_요청시_에러발생(){
        // given
        ReservationServiceDto.Request reservationRequest = null;
        String invalidToken = "invalid Token";

        // when & then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            reservationFacade.createReservation(reservationRequest, invalidToken);
        });
        assertEquals(ErrorCode.QUEUE_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void 대기열이_활성화되지않은_상태에서_토큰으로_API_요청시_에러발생(){
        // given
        ReservationServiceDto.Request reservationRequest = null;
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
            reservationFacade.createReservation(reservationRequest, testToken);
        });
        assertEquals(ErrorCode.QUEUE_NOT_ALLOWED, exception.getErrorCode());
    }

    /**
     * data.sql 을 통해 테스트 데이터를 미리 추가
     */
    @Test
    void 존재하지_않는_유저정보로_예약요청시_에러발생(){
        // given
        ReservationServiceDto.Request reservationRequest = new ReservationServiceDto.Request(
                999L,
                1L,
                1L,
                1L
        );
        // when & then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            reservationFacade.createReservation(reservationRequest, token);
        });
        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
    }
    @Test
    void 존재하지_않는_콘서트정보로_예약요청시_에러발생(){
        // given
        ReservationServiceDto.Request reservationRequest = new ReservationServiceDto.Request(
                1L,
                999L,
                1L,
                1L
        );
        // when & then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            reservationFacade.createReservation(reservationRequest, token);
        });
        assertEquals(ErrorCode.CONCERT_NOT_FOUND, exception.getErrorCode());
    }
    @Test
    void 존재하지_않는_콘서트스케줄정보로_예약요청시_에러발생(){
        // given
        ReservationServiceDto.Request reservationRequest = new ReservationServiceDto.Request(
                1L,
                1L,
                999L,
                1L
        );
        // when & then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            reservationFacade.createReservation(reservationRequest, token);
        });
        assertEquals(ErrorCode.CONCERT_SCHEDULE_NOT_FOUND, exception.getErrorCode());
    }
    @Test
    void 사용_불가능한_좌석번호로_예약요청시_에러발생(){
        // given
        ReservationServiceDto.Request reservationRequest = new ReservationServiceDto.Request(
                1L,
                1L,
                1L,
                26L
        );
        // when & then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            reservationFacade.createReservation(reservationRequest, token);
        });
        assertEquals(ErrorCode.CONCERT_SEAT_NOT_AVAILABLE, exception.getErrorCode());
    }

    @Test
    void 정상적으로_예약정보를_생성(){
        // given
        Long userId = 1L;
        Long concertId = 1L;
        Long concertScheduleId = 1L;
        Long seatId = 1L;
        ReservationServiceDto.Request reservationRequest = new ReservationServiceDto.Request(
                userId,
                concertId,
                concertScheduleId,
                seatId
        );
        // when
        ReservationServiceDto.Result result = reservationFacade.createReservation(reservationRequest, token);

        // then
        List<Reservation> reservationList = reservationRepository.findAll();

        assertNotNull(result);
        assertEquals(reservationList.size(), 1);
        assertEquals(result.reservationId(), reservationList.get(0).getId());
        assertEquals(result.concertId(), reservationList.get(0).getConcert().getId());
        assertEquals(result.concertName(), reservationList.get(0).getConcert().getTitle());
        assertEquals(result.seatNumber(), reservationList.get(0).getSeat().getSeatNumber());
        assertEquals(result.seatPrice(), reservationList.get(0).getSeat().getSeatPrice());
        assertEquals(result.reservationStatus(), reservationList.get(0).getStatus());
    }
}
