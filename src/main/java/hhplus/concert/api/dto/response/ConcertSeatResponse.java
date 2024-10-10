package hhplus.concert.api.dto.response;

import hhplus.concert.api.dto.ConcertSeatInfo;

import java.time.LocalDateTime;
import java.util.List;

public record ConcertSeatResponse(
        Long concertId,
        LocalDateTime concertAt,
        List<ConcertSeatInfo> seats
) {
}
