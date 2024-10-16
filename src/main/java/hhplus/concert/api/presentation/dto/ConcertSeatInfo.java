package hhplus.concert.api.presentation.dto;

import hhplus.concert.support.type.SeatStatus;

public record ConcertSeatInfo(
        Long seatId,
        int seatNumber,
        SeatStatus seatStatus,
        int SeatPrice
) {
}
