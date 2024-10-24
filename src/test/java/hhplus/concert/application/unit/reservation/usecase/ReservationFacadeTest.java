package hhplus.concert.application.unit.reservation.usecase;

import hhplus.concert.application.reservation.dto.ReservationServiceDto;
import hhplus.concert.application.reservation.usecase.ReservationFacade;
import hhplus.concert.domain.concert.components.ConcertService;
import hhplus.concert.domain.concert.models.Concert;
import hhplus.concert.domain.concert.models.ConcertSchedule;
import hhplus.concert.domain.concert.models.Seat;
import hhplus.concert.domain.queue.components.QueueService;
import hhplus.concert.domain.queue.models.Queue;
import hhplus.concert.domain.reservation.components.ReservationService;
import hhplus.concert.domain.reservation.models.Reservation;
import hhplus.concert.domain.user.components.UserService;
import hhplus.concert.domain.user.models.User;
import hhplus.concert.support.error.ErrorCode;
import hhplus.concert.support.error.exception.BusinessException;
import hhplus.concert.support.type.QueueStatus;
import hhplus.concert.support.type.ReservationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ReservationFacadeTest {
    @Mock
    private UserService userService;
    @Mock
    private QueueService queueService;
    @Mock
    private ConcertService concertService;
    @Mock
    private ReservationService reservationService;

    @InjectMocks
    private ReservationFacade reservationFacade;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private static final String TOKEN = "test_token";

    @Test
    void 토큰_검증시_만료된_대기열의_경우_예외발생(){
        // Given
        // mocking
        ReservationServiceDto.Request reservationRequest = mock(ReservationServiceDto.Request.class);
        Queue queue = mock(Queue.class);

        // stub
        when(queueService.findQueueByToken(TOKEN)).thenReturn(queue);
        when(queue.getStatus()).thenReturn(QueueStatus.EXPIRED); // 만료로 설정

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            reservationFacade.createReservation(reservationRequest, TOKEN);
        });
        assertEquals(ErrorCode.QUEUE_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void 정상적으로_좌석예약_요청을_생성한다(){
        // Given
        Long userId = 1L;
        Long concertId = 2L;
        Long concertScheduleId = 3L;
        Long seatId = 4L;
        Long reservationId = 5L;
        int seatNumber = 1;
        int seatPrice = 10000;
        ReservationStatus reservationStatus = ReservationStatus.COMPLETED;

        // mocking
        ReservationServiceDto.Request reservationRequest = mock(ReservationServiceDto.Request.class);
        User user = mock(User.class);
        Queue queue = mock(Queue.class);
        Concert concert = mock(Concert.class);
        ConcertSchedule concertSchedule = mock(ConcertSchedule.class);
        Seat seat1 = mock(Seat.class);
        Seat seat2 = mock(Seat.class);
        Reservation reservation = mock(Reservation.class);

        List<Seat> seats = Arrays.asList(seat1,seat2);

        // stub
        when(user.getId()).thenReturn(userId);
        when(concert.getId()).thenReturn(concertId);
        when(concert.getTitle()).thenReturn("Test Concert");
        when(concertSchedule.getId()).thenReturn(concertScheduleId);
        when(concertSchedule.getConcertAt()).thenReturn(LocalDateTime.now());
        when(seat1.getId()).thenReturn(seatId); // seat1을 예약할꺼라, seat1만 우선 mocking
        when(seat1.getSeatNumber()).thenReturn(seatNumber);
        when(seat1.getSeatPrice()).thenReturn(seatPrice);

        when(reservationRequest.userId()).thenReturn(userId);
        when(reservationRequest.concertId()).thenReturn(concertId);
        when(reservationRequest.concertScheduleId()).thenReturn(concertScheduleId);
        when(reservationRequest.seatId()).thenReturn(seatId);

        // 토큰 검증
        when(queueService.findQueueByToken(TOKEN)).thenReturn(queue);
        when(queue.getStatus()).thenReturn(QueueStatus.ACTIVE);

        // request 정보를 검증
        when(userService.findUserInfo(reservationRequest.userId())).thenReturn(user);
        when(concertService.getAvailableSeats(reservationRequest.concertId(), reservationRequest.concertScheduleId())).thenReturn(seats);

        // 예약 요청
        when(reservationService.createReservation(userId,concertId,concertScheduleId,seatId)).thenReturn(reservation);
        when(reservation.getId()).thenReturn(reservationId);
        when(reservation.getConcert()).thenReturn(concert);
        when(reservation.getSeat()).thenReturn(seat1);
        when(reservation.getSeat().getConcertSchedule()).thenReturn(concertSchedule);
        when(reservation.getStatus()).thenReturn(reservationStatus);

        // When
        ReservationServiceDto.Result result = reservationFacade.createReservation(reservationRequest, TOKEN);

        // Then
        assertNotNull(result);
        assertEquals(reservationId, result.reservationId());
        assertEquals(concertId, result.concertId());
        assertEquals("Test Concert", result.concertName());
        assertEquals(seatNumber, result.seatNumber());
        assertEquals(seatPrice, result.seatPrice());
        assertEquals(reservationStatus, result.reservationStatus());
    }
}