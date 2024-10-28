package hhplus.concert.domain.reservation.components;

import hhplus.concert.domain.concert.models.Concert;
import hhplus.concert.domain.concert.models.ConcertSchedule;
import hhplus.concert.domain.concert.models.Seat;
import hhplus.concert.domain.concert.repositories.ConcertRepository;
import hhplus.concert.domain.concert.repositories.ConcertScheduleRepository;
import hhplus.concert.domain.concert.repositories.SeatRepository;
import hhplus.concert.domain.reservation.models.Reservation;
import hhplus.concert.domain.reservation.repositories.ReservationRepository;
import hhplus.concert.domain.user.models.User;
import hhplus.concert.domain.user.repositories.UserRepository;
import hhplus.concert.support.type.ConcertStatus;
import hhplus.concert.support.type.ReservationStatus;
import hhplus.concert.support.type.SeatStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReservationServiceTest {
    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ConcertRepository concertRepository;
    @Mock
    private ConcertScheduleRepository concertScheduleRepository;
    @Mock
    private SeatRepository seatRepository;

    @InjectMocks
    private ReservationService reservationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // 상수 정의
    private static final String TOKEN = "test_token";
    public static final Concert AVAILABLE_CONCERT;
    public static final ConcertSchedule VALID_SCHEDULE;
    public static final Seat SEAT1;
    public static final Seat SEAT2;
    public static final Reservation RESERVATION1;
    public static final Reservation RESERVATION2;
    static {
        AVAILABLE_CONCERT = new Concert(
                "test_concert",
                "available_concert",
                ConcertStatus.AVAILABLE
        );

        VALID_SCHEDULE = new ConcertSchedule(
                AVAILABLE_CONCERT,
                LocalDateTime.now().plusDays(2),
                LocalDateTime.now().minusDays(1)
        );

        SEAT1 = new Seat(
                VALID_SCHEDULE,
                1,
                10000,
                SeatStatus.AVAILABLE
        );

        SEAT2 = new Seat(
                VALID_SCHEDULE,
                2, // seatNumber
                10000,
                SeatStatus.AVAILABLE
        );

        // Reservation 객체 생성
        RESERVATION1 = new Reservation(
                new User("user1"),
                AVAILABLE_CONCERT,
                SEAT1,
                ReservationStatus.PENDING,
                LocalDateTime.now()
        );

        RESERVATION2 = new Reservation(
                new User("user2"),
                AVAILABLE_CONCERT,
                SEAT2,
                ReservationStatus.PENDING,
                LocalDateTime.now()
        );
        // 리플렉션을 사용하여 ID 값을 설정
        setId(AVAILABLE_CONCERT, 1L);
        setId(VALID_SCHEDULE, 1L);
        setId(SEAT1, 1L);
        setId(SEAT2, 2L);
        setId(RESERVATION1, 1L);
        setId(RESERVATION2, 2L);
    }

    // 리플렉션을 사용하여 private 필드에 값을 주입하는 메서드
    private static void setId(Object entity, Long id) {
        try {
            // 필드 이름이 "id"인 private 필드에 접근
            Field field = entity.getClass().getDeclaredField("id");
            field.setAccessible(true); // private 필드에 접근 가능하도록 설정
            field.set(entity, id); // 해당 필드에 id 값을 설정
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void 콘서트_예약을_생성한다(){
        // Given
        Long userId = 1L;
        Long concertId = 1L;
        Long concertScheduleId = 1L;
        Long seat1Id = 1L;
        User user = new User("jkcho");

        when(userRepository.findById(userId)).thenReturn(user);
        when(concertRepository.findById(concertId)).thenReturn(AVAILABLE_CONCERT);
        when(concertScheduleRepository.findById(concertScheduleId)).thenReturn(VALID_SCHEDULE);
        when(seatRepository.findById(seat1Id)).thenReturn(SEAT1);

        // When
        Reservation result = reservationService.createReservation(userId, concertId, concertScheduleId, seat1Id);

        // Then
        verify(reservationRepository).save(result);
        verify(seatRepository).save(SEAT1);
    }
    @Test
    void 예약_후_5분이내_결제가_이루어지지않은_만료된_예약건을_조회하여_좌석상태와_예약상태를_변경한다() {
        // Given
        List<Reservation> expiredReservations = List.of(RESERVATION1, RESERVATION2);

        when(reservationRepository.findExpiredReservations(
                eq(ReservationStatus.PENDING),
                any(LocalDateTime.class)
        )).thenReturn(expiredReservations);

        // When
        reservationService.cancelReservations();

        // Then
        verify(reservationRepository).updateAllStatus(
                expiredReservations.stream()
                        .map(Reservation::getId)
                        .toList(),
                ReservationStatus.CANCELLED
        );
        verify(seatRepository).updateAllStatusByIds(
                expiredReservations.stream()
                        .map(reservation -> reservation.getSeat().getId())
                        .toList(),
                SeatStatus.AVAILABLE
        );
    }
}