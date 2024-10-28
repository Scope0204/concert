package hhplus.concert.application.reservation.usecase;

import hhplus.concert.application.reservation.dto.ReservationServiceDto;
import hhplus.concert.domain.concert.components.ConcertService;
import hhplus.concert.domain.concert.models.Seat;
import hhplus.concert.domain.queue.components.QueueService;
import hhplus.concert.domain.queue.models.Queue;
import hhplus.concert.domain.reservation.components.ReservationService;
import hhplus.concert.domain.reservation.models.Reservation;
import hhplus.concert.domain.user.components.UserService;
import hhplus.concert.support.error.ErrorCode;
import hhplus.concert.support.error.exception.BusinessException;
import hhplus.concert.support.type.QueueStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReservationFacade {
    private final UserService userService;
    private final QueueService queueService;
    private final ConcertService concertService;
    private final ReservationService reservationService;

    public ReservationFacade(UserService userService, QueueService queueService, ConcertService concertService, ReservationService reservationService) {
        this.userService = userService;
        this.queueService = queueService;
        this.concertService = concertService;
        this.reservationService = reservationService;
    }

    /**
     * 좌석 예약 요청 생성합니다.
     * 1. 토큰을 통해 대기열 상태를 검증하도록 합니다.
     * 2. 존재 여부를 확인합니다.
     * 3. 예약을 생성하도록 합니다.
     */
    @Transactional
    public ReservationServiceDto.Result createReservation(ReservationServiceDto.Request reservationRequest, String token) {
        validateQueueStatus(token);

        validateReservationRequest(reservationRequest);

        Reservation reservation = reservationService.createReservation(
                reservationRequest.userId(),
                reservationRequest.concertId(),
                reservationRequest.concertScheduleId(),
                reservationRequest.seatId()
        );
        return new ReservationServiceDto.Result(
                reservation.getId(),
                reservation.getConcert().getId(),
                reservation.getConcert().getTitle(),
                reservation.getSeat().getConcertSchedule().getConcertAt(),
                reservation.getSeat().getSeatNumber(),
                reservation.getSeat().getSeatPrice(),
                reservation.getStatus()
        );
    }

    /**
     * 스케줄러를 통해, 5분이 지나도 결제가 완료되지않은 예약 건이 있는 경우를 확인하여 다음 처럼 상태를 변경합니다.
     * ReservationStatus CANCELED
     * SeatStatus AVAILABLE
     */
    @Transactional
    public void cancelReservationsAndResetSeats(){
        reservationService.cancelReservations();
    }


    private void validateQueueStatus(String token){
        Queue queue = queueService.findQueueByToken(token);
        if(queue.getStatus() != QueueStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.QUEUE_NOT_ALLOWED);
        }
    }

    // reservationRequest 정보를 검증
    private void validateReservationRequest(ReservationServiceDto.Request reservationRequest){
        userService.findUserInfo(reservationRequest.userId()); // 유저 정보를 조회
        List<Seat> availableSeats = concertService.getAvailableSeats(reservationRequest.concertId(), reservationRequest.concertScheduleId()); // 콘서트와, 스케줄 정보를 통해 사용가능한 좌석정보 조회
        boolean isSeatAvailable = availableSeats.stream()
                .anyMatch(seat -> seat.getId().equals(reservationRequest.seatId())); // 좌석 ID가 리스트에 존재하는지 체크
        if (!isSeatAvailable) {
            throw new BusinessException(ErrorCode.CONCERT_SEAT_NOT_AVAILABLE); // 예외 발생
        }
    }
}
