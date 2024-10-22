package hhplus.concert.application.concert.dto;

import hhplus.concert.support.type.SeatStatus;

import java.time.LocalDateTime;
import java.util.List;

public class ConcertServiceDto {
    public record Concert(
            Long concertId,
            String title,
            String description
    ) {
    }

    public record Schedule(
            Long concertId,
            List<ConcertSchedule> concertSchedules
    ) {
    }

    public record ConcertSchedule (
            Long scheduleId,
            LocalDateTime concertAt,
            LocalDateTime reservationAt
    ) {
    }

    public record AvailableSeat(
            Long concertId,
            List<Seat> seats) {
    }

    public record Seat(
            Long seatId,
            int seatNumber,
            SeatStatus seatStatus,
            int seatPrice
    ) {
    }
}
