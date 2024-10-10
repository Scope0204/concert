package hhplus.concert.api.dto.request;

import hhplus.concert.common.type.ReservationStatus;

public record ReservationRequest(
        Long userId,
        Long concertId,
        Long scheduleId,
        ReservationStatus status
) {
}
