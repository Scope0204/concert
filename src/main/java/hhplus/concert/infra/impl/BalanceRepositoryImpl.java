package hhplus.concert.infra.impl;

import hhplus.concert.domain.balance.models.Balance;
import hhplus.concert.domain.balance.repositories.BalanceRepository;
import hhplus.concert.infra.jpa.JpaBalanceRepository;
import hhplus.concert.support.error.exception.BalanceException;
import org.springframework.stereotype.Repository;

@Repository
public class BalanceRepositoryImpl implements BalanceRepository {
    private final JpaBalanceRepository jpaBalanceRepository;

    public BalanceRepositoryImpl(JpaBalanceRepository jpaBalanceRepository) {
        this.jpaBalanceRepository = jpaBalanceRepository;
    }

    @Override
    public Balance findByUserId(Long userId) {
        Balance balance = jpaBalanceRepository.findByUserId(userId);
        if(balance == null) {
            throw new BalanceException.BalanceNotFound();
        }
        return balance;
    }

    @Override
    public void save(Balance balance) {
        jpaBalanceRepository.save(balance);
    }


}