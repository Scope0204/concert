package hhplus.concert.api.presentation.response;

import hhplus.concert.application.reservation.dto.ReservationServiceDto;
import hhplus.concert.support.type.ReservationStatus;

import java.time.LocalDateTime;

public class ReservationResponse {
    public record Result(
            Long reservationId,
            Long concertId,
            String concertName,
            LocalDateTime concertAt,
            int seatNumber,
            int seatPrice,
            ReservationStatus reservationStatus
    ){
        public static Result from(ReservationServiceDto.Result resultDto) {
            return new Result(
                    resultDto.reservationId(),
                    resultDto.concertId(),
                    resultDto.concertName(),
                    resultDto.concertAt(),
                    resultDto.seatNumber(),
                    resultDto.seatPrice(),
                    resultDto.reservationStatus()
            );
        }
    }
}
