package hhplus.concert.infra.impl;

import hhplus.concert.domain.concert.models.Seat;
import hhplus.concert.domain.concert.repositories.SeatRepository;
import hhplus.concert.infra.jpa.JpaSeatRepository;
import hhplus.concert.support.error.exception.ConcertException;
import hhplus.concert.support.type.SeatStatus;
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

    @Override
    public Seat findById(Long seatId) {
        return jpaSeatRepository.findById(seatId)
                .orElseThrow(() -> new ConcertException.ConcertSeatNotFound());
    }

    @Override
    public void updateStatusById(Long seatId, SeatStatus seatStatus) {
        jpaSeatRepository.updateStatusById(seatId, seatStatus);
    }

    @Override
    public void updateAllStatusByIds(List<Long> seatIds, SeatStatus seatStatus) {
        jpaSeatRepository.updateAllStatusByIds(seatIds, seatStatus);
    }
}
