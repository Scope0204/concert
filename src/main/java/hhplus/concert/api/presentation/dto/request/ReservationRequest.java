package hhplus.concert.api.presentation.dto.request;

import hhplus.concert.support.type.ReservationStatus;

public record ReservationRequest(
        Long userId,
        Long concertId,
        Long scheduleId,
        ReservationStatus status
) {
}
