package hhplus.concert.application.concert.usecase;

import hhplus.concert.application.concert.dto.ConcertServiceDto;
import hhplus.concert.domain.concert.components.ConcertService;
import hhplus.concert.domain.concert.models.Concert;
import hhplus.concert.domain.concert.models.ConcertSchedule;
import hhplus.concert.domain.concert.models.Seat;
import hhplus.concert.domain.queue.components.QueueService;
import hhplus.concert.domain.queue.models.Queue;
import hhplus.concert.domain.user.models.User;
import hhplus.concert.support.error.ErrorCode;
import hhplus.concert.support.error.exception.BusinessException;
import hhplus.concert.support.type.ConcertStatus;
import hhplus.concert.support.type.QueueStatus;
import hhplus.concert.support.type.SeatStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

class ConcertFacadeTest {
    @Mock
    private QueueService queueService;

    @Mock
    private ConcertService concertService;

    @InjectMocks
    private ConcertFacade concertFacade;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private static final String TOKEN = "test_token";

    @Test
    void 예약가능한_콘서트_리스트를_조회한다() {
        // Given
        User mockUser = new User("scope");
        Queue activeQueue = Queue.builder()
                .token(TOKEN)
                .user(mockUser)
                .status(QueueStatus.ACTIVE)
                .build();
        when(queueService.findQueueByToken(TOKEN)).thenReturn(activeQueue);

        Concert concert = new Concert("콘서트1", "1", ConcertStatus.AVAILABLE);
        Concert concert2 = new Concert("콘서트2", "2", ConcertStatus.AVAILABLE);
        Concert concert3 = new Concert("콘서트3", "3", ConcertStatus.AVAILABLE);

        when(concertService.getAvailableConcerts()).thenReturn(Arrays.asList(concert,concert2,concert3));

        // When
        List<ConcertServiceDto.Concert> availableConcerts = concertFacade.getAvailableConcerts(TOKEN);

        // Then
        assertEquals(3, availableConcerts.size());
        assertEquals(concert.getId(), availableConcerts.get(0).concertId());
        assertEquals(concert.getTitle(), availableConcerts.get(0).title());
        assertEquals(concert.getDescription(), availableConcerts.get(0).description());
    }

    @Test
    void 특정_콘서트의_예약가능한_일정_리스트를_조회한다() {
        // Given
        Long concertId = 1L;
        User mockUser = new User("scope");
        Queue activeQueue = Queue.builder()
                .token(TOKEN)
                .user(mockUser)
                .status(QueueStatus.ACTIVE)
                .build();

        when(queueService.findQueueByToken(TOKEN)).thenReturn(activeQueue);

        Concert concert = new Concert("빅뱅 콘서트", "1", ConcertStatus.AVAILABLE);
        ConcertSchedule schedule1 = new ConcertSchedule(concert, LocalDateTime.now().plusHours(2), LocalDateTime.now().minusHours(1)); // 예약 가능
        ConcertSchedule schedule2 = new ConcertSchedule(concert, LocalDateTime.now().minusHours(1), LocalDateTime.now()); // 예약 불가능
        when(concertService.getAvailableSchedulesForConcert(concertId)).thenReturn(Arrays.asList(schedule1));

        // When
        ConcertServiceDto.Schedule availableSchedules = concertFacade.getAvailableSchedulesForConcert(concertId, TOKEN);

        // Then
        assertEquals(concertId, availableSchedules.concertId());
        assertEquals(1, availableSchedules.concertSchedules().size());
        assertEquals(schedule1.getId(), availableSchedules.concertSchedules().get(0).scheduleId());
    }

    @Test
    void 예약가능한_좌석정보를_조회한다() {
        // Given
        Long concertId = 1L;
        Long scheduleId = 1L;
        User mockUser = new User("scope");
        Queue activeQueue = Queue.builder()
                .token(TOKEN)
                .user(mockUser)
                .status(QueueStatus.ACTIVE)
                .build();
        when(queueService.findQueueByToken(TOKEN)).thenReturn(activeQueue);

        Concert concert = new Concert("빅뱅 콘서트", "1", ConcertStatus.AVAILABLE);
        ConcertSchedule schedule = new ConcertSchedule(concert, LocalDateTime.now().plusHours(2), LocalDateTime.now().minusHours(1)); // 예약 가능
        Seat seat1 = new Seat(schedule, 1, 10000, SeatStatus.AVAILABLE);
        Seat seat2 = new Seat(schedule, 2, 20000, SeatStatus.AVAILABLE);
        Seat seat3 = new Seat(schedule, 3, 30000, SeatStatus.AVAILABLE);

        when(concertService.getAvailableSeats(concertId, scheduleId)).thenReturn(Arrays.asList(seat1,seat2,seat3));

        // When
        ConcertServiceDto.AvailableSeat availableSeats = concertFacade.getAvailableSeats(concertId, scheduleId, TOKEN);

        // Then
        assertEquals(concertId, availableSeats.concertId());
        assertEquals(3, availableSeats.seats().size());
        assertEquals(seat1.getId(), availableSeats.seats().get(0).seatId());
    }

    @Test
    void 대기열이_비활성인경우_예외발생() {
        // Given
        User mockUser = new User("scope");
        Queue expiredQueue = Queue.builder()
                .token(TOKEN)
                .user(mockUser)
                .status(QueueStatus.EXPIRED)
                .build();
        when(queueService.findQueueByToken(TOKEN)).thenReturn(expiredQueue);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            concertFacade.getAvailableConcerts(TOKEN);
        });
        assertEquals(ErrorCode.QUEUE_NOT_FOUND, exception.getErrorCode());
    }

}