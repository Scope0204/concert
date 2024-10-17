package hhplus.concert.domain.concert.repositories;

import hhplus.concert.domain.concert.models.Seat;

import java.util.List;

public interface SeatRepository {
    List<Seat> findAllByConcertScheduleId(Long concertScheduleId);
}
