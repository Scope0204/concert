package hhplus.concert.api.dto;

import java.time.LocalDateTime;

public record ConcertScheduleInfo(
        Long scheduleId,
        LocalDateTime concertAt,
        LocalDateTime reservationAt
) {
}
