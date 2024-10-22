package hhplus.concert.domain.concert.repositories;

import hhplus.concert.domain.concert.models.Concert;

import java.util.List;

public interface ConcertRepository {
    List<Concert> findAll();
    Concert findById(Long concertId);
}
