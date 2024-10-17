package hhplus.concert.infra.jpa;

import hhplus.concert.domain.concert.models.ConcertSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaConcertScheduleRepository extends JpaRepository<ConcertSchedule, Long> {
    List<ConcertSchedule> findAllByConcertId(Long concertId);
}
