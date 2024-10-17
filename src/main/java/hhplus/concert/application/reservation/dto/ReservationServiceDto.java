package hhplus.concert.application.reservation.dto;

import hhplus.concert.support.type.ReservationStatus;

import java.time.LocalDateTime;

public class ReservationServiceDto {
    public record Request(
            Long userId,
            Long concertId,
            Long concertScheduleId,
            Long seatId
    ) {
    }

    public record Result(
            Long reservationId,
            Long concertId,
            String concertName,
            LocalDateTime concertAt,
            int seatNumber,
            int seatPrice,
            ReservationStatus reservationStatus
    ) {
    }
}
