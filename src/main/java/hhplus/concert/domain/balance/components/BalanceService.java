package hhplus.concert.domain.balance.components;

import hhplus.concert.domain.balance.models.Balance;
import hhplus.concert.domain.balance.repositories.BalanceRepository;
import org.springframework.stereotype.Service;

@Service
public class BalanceService {
    private final BalanceRepository balanceRepository;

    public BalanceService(BalanceRepository balanceRepository) {
        this.balanceRepository = balanceRepository;
    }

    public Balance getBalanceByUserId(Long userId) {
        Balance balance = balanceRepository.findByUserId(userId);
        return balance;
    }

    public Balance charge(Long userId, int amount) {
        //Balance balance = balanceRepository.findByUserId(userId);
        Balance balance = balanceRepository.findByUserIdWithPessimisticLock(userId); // 비관적 락을 적용
        balance.updateAmount(amount);
        balanceRepository.save(balance);

        return new Balance(
                balance.getUser(),
                balance.getAmount(),
                balance.getUpdatedAt()
        );
    }
}
