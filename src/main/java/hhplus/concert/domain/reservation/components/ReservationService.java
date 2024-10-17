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
import hhplus.concert.support.type.ReservationStatus;
import hhplus.concert.support.type.SeatStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final ConcertRepository concertRepository;
    private final ConcertScheduleRepository concertScheduleRepository;
    private final SeatRepository seatRepository;


    public ReservationService(ReservationRepository reservationRepository, UserRepository userRepository, ConcertRepository concertRepository, ConcertScheduleRepository concertScheduleRepository, SeatRepository seatRepository) {
        this.reservationRepository = reservationRepository;
        this.userRepository = userRepository;
        this.concertRepository = concertRepository;
        this.concertScheduleRepository = concertScheduleRepository;
        this.seatRepository = seatRepository;
    }

    /**
     * 콘서트 좌석 예약 생성
     * 예약 후 해당 좌석은 이용 불가능 하도록 상태 업데이트
     * @param userId
     * @param concertId
     * @param concertScheduleId
     * @param seatId
     * @return Reservation
     */
    @Transactional
    public Reservation createReservation(Long userId, Long concertId, Long concertScheduleId, Long seatId) {
        User user = userRepository.findById(userId);
        Concert concert = concertRepository.findById(concertId);
        ConcertSchedule concertSchedule = concertScheduleRepository.findById(concertScheduleId);
        Seat seat = seatRepository.findById(seatId);

        Reservation reservation = new Reservation(
                user,
                concert,
                seat,
                ReservationStatus.PENDING,
                LocalDateTime.now()
        );
        reservationRepository.save(reservation);

        // 해당 좌석은 예약 불가능 하도록 업데이트
        seatRepository.updateStatusById(seatId, SeatStatus.UNAVAILABLE);

        return reservation;
    }

    /**
     * 좌석 예약 상태를 취소한다.
     * 1. 예약 후 5분 이내 결제가 완료되지않은 예약 건을 조회한다.
     * 2. 조회 된 예약 건들의 상태를 변경한다(CANCELED)
     * 3. 조회 된 예약 좌석의 상태도 변경한다(AVAILABLE)
     */
    @Transactional
    public void cancelReservations() {
        List<Reservation> expiredReservations = reservationRepository.findExpiredReservations(
                ReservationStatus.PENDING,
                LocalDateTime.now().minusMinutes(5)
        );

        reservationRepository.updateAllStatus(
                expiredReservations.stream()
                        .map(Reservation::getId)
                        .toList(),
                ReservationStatus.CANCELLED
        );

        seatRepository.updateAllStatusByIds(
                expiredReservations.stream()
                        .map(reservation -> reservation.getSeat().getId())
                        .toList(),
                SeatStatus.AVAILABLE
        );
    }
}
