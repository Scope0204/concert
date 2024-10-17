package hhplus.concert.infra.impl;

import hhplus.concert.domain.concert.models.ConcertSchedule;
import hhplus.concert.domain.concert.repositories.ConcertScheduleRepository;
import hhplus.concert.infra.jpa.JpaConcertScheduleRepository;
import hhplus.concert.support.error.exception.ConcertException;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ConcertScheduleRepositoryImpl implements ConcertScheduleRepository {
    private final JpaConcertScheduleRepository jpaConcertScheduleRepository;

    public ConcertScheduleRepositoryImpl(JpaConcertScheduleRepository jpaConcertScheduleRepository) {
        this.jpaConcertScheduleRepository = jpaConcertScheduleRepository;
    }

    @Override
    public List<ConcertSchedule> findAllByConcertId(Long concertId) {
        return jpaConcertScheduleRepository.findAllByConcertId(concertId);
    }

    @Override
    public ConcertSchedule findById(Long concertScheduleId) {
        return jpaConcertScheduleRepository.findById(concertScheduleId)
                .orElseThrow(() -> new ConcertException.ConcertScheduleNotFound() {
                });
    }
}
