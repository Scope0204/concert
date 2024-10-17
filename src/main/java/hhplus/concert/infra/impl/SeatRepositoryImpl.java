package hhplus.concert.infra.impl;

import hhplus.concert.domain.concert.models.Seat;
import hhplus.concert.domain.concert.repositories.SeatRepository;
import hhplus.concert.infra.jpa.JpaSeatRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class SeatRepositoryImpl implements SeatRepository {
    private final JpaSeatRepository jpaSeatRepository;

    public SeatRepositoryImpl(JpaSeatRepository jpaSeatRepository) {
        this.jpaSeatRepository = jpaSeatRepository;
    }

    @Override
    public List<Seat> findAllByConcertScheduleId(Long concertScheduleId) {
        return jpaSeatRepository.findAllByConcertScheduleId(concertScheduleId);
    }
}
