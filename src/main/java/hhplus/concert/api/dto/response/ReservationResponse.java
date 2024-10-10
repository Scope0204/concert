package hhplus.concert.api.dto.response;

import hhplus.concert.common.type.ReservationStatus;

import java.time.LocalDateTime;

public record ReservationResponse(
        Long reservationId,
        Long concertId,
        String concertName,
        LocalDateTime concertAt,
        int SeatNumber,
        int SeatPrice,
        ReservationStatus reservationStatus
) {
}
