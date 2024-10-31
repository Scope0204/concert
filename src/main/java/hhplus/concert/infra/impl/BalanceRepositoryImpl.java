package hhplus.concert.infra.impl;

import hhplus.concert.domain.balance.models.Balance;
import hhplus.concert.domain.balance.repositories.BalanceRepository;
import hhplus.concert.infra.jpa.JpaBalanceRepository;
import org.springframework.stereotype.Repository;

@Repository
public class BalanceRepositoryImpl implements BalanceRepository {
    private final JpaBalanceRepository jpaBalanceRepository;

    public BalanceRepositoryImpl(JpaBalanceRepository jpaBalanceRepository) {
        this.jpaBalanceRepository = jpaBalanceRepository;
    }

    @Override
    public Balance findByUserId(Long userId) {
        return jpaBalanceRepository.findByUserId(userId);
    }

    @Override
    public Balance findByUserIdWithPessimisticLock(Long userId) {
        return jpaBalanceRepository.findByUserIdWithPessimisticLock(userId);
    }

    @Override
    public void save(Balance balance) {
        jpaBalanceRepository.save(balance);
    }

}
