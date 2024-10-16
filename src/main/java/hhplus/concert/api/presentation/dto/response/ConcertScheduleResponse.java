package hhplus.concert.api.presentation.dto.response;

import hhplus.concert.api.presentation.dto.ConcertScheduleInfo;

import java.util.List;

public record ConcertScheduleResponse(
        Long concertId,
        List<ConcertScheduleInfo> events
) {
}
