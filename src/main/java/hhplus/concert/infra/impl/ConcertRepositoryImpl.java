package hhplus.concert.infra.impl;

import hhplus.concert.domain.concert.models.Concert;
import hhplus.concert.domain.concert.repositories.ConcertRepository;
import hhplus.concert.infra.jpa.JpaConcertRepository;
import hhplus.concert.support.error.exception.ConcertException;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ConcertRepositoryImpl implements ConcertRepository {
    private final JpaConcertRepository jpaConcertRepository;

    public ConcertRepositoryImpl(JpaConcertRepository jpaConcertRepository) {
        this.jpaConcertRepository = jpaConcertRepository;
    }

    @Override
    public List<Concert> findAll() {
        return jpaConcertRepository.findAll();
    }

    @Override
    public Concert findById(Long concertId) {
        return jpaConcertRepository.findById(concertId)
                .orElseThrow(() -> new ConcertException.ConcertNotFound());
    }
}
