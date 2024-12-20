package hhplus.concert.domain.concert.repositories;

import hhplus.concert.domain.concert.models.Seat;
import hhplus.concert.support.type.SeatStatus;

import java.util.List;

public interface SeatRepository {
    List<Seat> findAllByConcertScheduleId(Long concertScheduleId);
    Seat findById(Long seatId);
    Seat findByIdWithPessimisticLock(Long seatId);
    void updateAllStatusByIds(List<Long> seatIds, SeatStatus seatStatus);
    void save(Seat seat);
}
