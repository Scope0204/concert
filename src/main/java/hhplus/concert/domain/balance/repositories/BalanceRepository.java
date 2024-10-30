package hhplus.concert.domain.balance.repositories;

import hhplus.concert.domain.balance.models.Balance;

public interface BalanceRepository {
    Balance findByUserId(Long userId);
    Balance findByUserIdWithPessimisticLock(Long userId);

    void save(Balance balance);
}
