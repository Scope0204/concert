package hhplus.concert.infra.jpa;

import hhplus.concert.domain.balance.models.Balance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JpaBalanceRepository extends JpaRepository<Balance, Long> {
    @Query("SELECT b FROM Balance b WHERE b.user = :userId")
    Balance findByUserId(@Param("userId") Long userId);
}

