package hhplus.concert.api.presentation.dto.response;

import hhplus.concert.api.presentation.dto.ConcertSeatInfo;

import java.time.LocalDateTime;
import java.util.List;

public record ConcertSeatResponse(
        Long concertId,
        LocalDateTime concertAt,
        List<ConcertSeatInfo> seats
) {
}
