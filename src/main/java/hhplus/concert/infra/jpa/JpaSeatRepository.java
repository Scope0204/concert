package hhplus.concert.infra.jpa;

import hhplus.concert.domain.concert.models.Seat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface JpaSeatRepository extends JpaRepository<Seat, Long> {
    List<Seat> findAllByConcertScheduleId(Long concertScheduleId);
}
