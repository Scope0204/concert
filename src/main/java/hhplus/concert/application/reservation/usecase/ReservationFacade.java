package hhplus.concert.application.reservation.usecase;

import hhplus.concert.application.reservation.dto.ReservationServiceDto;
import hhplus.concert.domain.queue.components.QueueService;
import hhplus.concert.domain.queue.models.Queue;
import hhplus.concert.domain.reservation.components.ReservationService;
import hhplus.concert.domain.reservation.models.Reservation;
import hhplus.concert.support.annotation.DistributedLock;
import hhplus.concert.support.error.ErrorCode;
import hhplus.concert.support.error.exception.BusinessException;
import hhplus.concert.support.type.QueueStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class ReservationFacade {
    private final QueueService queueService;
    private final ReservationService reservationService;

    public ReservationFacade(QueueService queueService, ReservationService reservationService) {
        this.queueService = queueService;
        this.reservationService = reservationService;
    }


    /**
     * 좌석 예약 요청 생성합니다.
     * 1. 토큰을 통해 대기열 상태를 검증하도록 합니다.
     * 2. 좌석 예약 요청을 생성합니다.
     */
    @DistributedLock(key = "#reservationRequest.seatId")
    public ReservationServiceDto.Result createReservation(ReservationServiceDto.Request reservationRequest, String token) {

        validateQueueStatus(token);

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
}
