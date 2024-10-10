package hhplus.concert.api.dto;

import hhplus.concert.common.type.SeatStatus;

public record ConcertSeatInfo(
        Long seatId,
        int seatNumber,
        SeatStatus seatStatus,
        int SeatPrice
) {
}
