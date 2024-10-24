package hhplus.concert.domain.concert.repositories;

import hhplus.concert.domain.concert.models.ConcertSchedule;

import java.util.List;

public interface ConcertScheduleRepository {
    List<ConcertSchedule> findAllByConcertId(Long concertId);
    ConcertSchedule findById(Long concertScheduleId);
    void save(ConcertSchedule concertSchedule);
}
