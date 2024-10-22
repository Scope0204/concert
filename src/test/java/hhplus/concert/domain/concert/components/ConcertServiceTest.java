package hhplus.concert.domain.concert.components;

import hhplus.concert.domain.concert.models.Concert;
import hhplus.concert.domain.concert.models.ConcertSchedule;
import hhplus.concert.domain.concert.models.Seat;
import hhplus.concert.domain.concert.repositories.ConcertRepository;
import hhplus.concert.domain.concert.repositories.ConcertScheduleRepository;
import hhplus.concert.domain.concert.repositories.SeatRepository;
import hhplus.concert.support.error.exception.BusinessException;
import hhplus.concert.support.type.ConcertStatus;
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

class ConcertServiceTest {
    @Mock
    private ConcertRepository concertRepository;

    @Mock
    private ConcertScheduleRepository concertScheduleRepository;

    @Mock
    private SeatRepository seatRepository;

    @InjectMocks
    private ConcertService concertService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void 예약가능한_콘서트_리스트만을_조회한다() {
        // Given
        Concert concert1 = new Concert("빅뱅 콘서트", "1", ConcertStatus.AVAILABLE);
        Concert concert2 = new Concert("아이유 콘서트", "2", ConcertStatus.UNAVAILABLE);
        when(concertRepository.findAll()).thenReturn(Arrays.asList(concert1, concert2));

        // When
        List<Concert> availableConcerts = concertService.getAvailableConcerts();

        // Then
        assertEquals(1, availableConcerts.size());
        assertEquals(concert1, availableConcerts.get(0));
    }

    @Test
    void 예약가능한_콘서트_일정_리스트를_조회한다() {
        // Given
        Long concertId = 1L;
        Concert concert = new Concert("빅뱅 콘서트", "1", ConcertStatus.AVAILABLE);
        ConcertSchedule schedule1 = new ConcertSchedule(concert, LocalDateTime.now().plusHours(2), LocalDateTime.now().minusHours(1)); // 예약 가능
        ConcertSchedule schedule2 = new ConcertSchedule(concert, LocalDateTime.now().minusHours(1), LocalDateTime.now()); // 예약 불가능

        when(concertRepository.findById(concertId)).thenReturn(concert);
        when(concertScheduleRepository.findAllByConcertId(concertId)).thenReturn(Arrays.asList(schedule1, schedule2));

        // When
        List<ConcertSchedule> availableSchedules = concertService.getAvailableSchedulesForConcert(concertId);

        // Then
        assertEquals(1, availableSchedules.size());
        assertEquals(schedule1, availableSchedules.get(0));
    }

    @Test
    void 예약가능한_좌석정보를_조회한다() {
        // Given
        Long concertId = 1L;
        Long concertScheduleId = 1L;
        Concert concert = new Concert("빅뱅 콘서트", "1", ConcertStatus.AVAILABLE);
        ConcertSchedule concertSchedule = new ConcertSchedule(concert, LocalDateTime.now().plusHours(2), LocalDateTime.now().minusHours(1));
        Seat seat1 = new Seat(concertSchedule, 1, 10000, SeatStatus.AVAILABLE);
        Seat seat2 = new Seat(concertSchedule, 2, 20000, SeatStatus.UNAVAILABLE);

        when(concertRepository.findById(concertId)).thenReturn(concert);
        when(concertScheduleRepository.findById(concertScheduleId)).thenReturn(concertSchedule);
        when(seatRepository.findAllByConcertScheduleId(concertScheduleId)).thenReturn(Arrays.asList(seat1, seat2));

        // When
        List<Seat> availableSeats = concertService.getAvailableSeats(concertId, concertScheduleId);

        // Then
        assertEquals(1, availableSeats.size());
        assertEquals(seat1, availableSeats.get(0));
    }

    @Test
    void 예약불가능한_콘서트일때_예외를_리턴() {
        // Given
        Long concertId = 1L;
        Concert concert = new Concert("아이유 콘서트", "2", ConcertStatus.UNAVAILABLE);
        when(concertRepository.findById(concertId)).thenReturn(concert);

        // When & Then
        assertThrows(BusinessException.class, () -> concertService.getAvailableSchedulesForConcert(concertId));
    }
}