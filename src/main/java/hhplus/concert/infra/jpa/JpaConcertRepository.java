package hhplus.concert.infra.jpa;

import hhplus.concert.domain.concert.models.Concert;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaConcertRepository extends JpaRepository<Concert, Long> {
}
