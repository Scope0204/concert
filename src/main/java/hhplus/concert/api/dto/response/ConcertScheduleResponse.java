package hhplus.concert.api.dto.response;

import hhplus.concert.api.dto.ConcertScheduleInfo;

import java.util.List;

public record ConcertScheduleResponse(
        Long concertId,
        List<ConcertScheduleInfo> events
) {
}
