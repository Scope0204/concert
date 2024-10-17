package hhplus.concert.domain.concert.repositories;

import hhplus.concert.domain.concert.models.ConcertSchedule;

import java.util.List;
import java.util.Optional;

public interface ConcertScheduleRepository {
    List<ConcertSchedule> findAllByConcertId(Long concertId);
    Optional<ConcertSchedule> findById(Long concertScheduleId);
}
