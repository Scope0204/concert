package hhplus.concert.domain.balance.components;

import hhplus.concert.domain.balance.models.Balance;
import hhplus.concert.domain.balance.repositories.BalanceRepository;
import hhplus.concert.domain.user.models.User;
import hhplus.concert.domain.user.repositories.UserRepository;
import hhplus.concert.support.error.exception.BalanceException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class BalanceService {
    private final UserRepository userRepository;
    private final BalanceRepository balanceRepository;

    public BalanceService(UserRepository userRepository, BalanceRepository balanceRepository) {
        this.userRepository = userRepository;
        this.balanceRepository = balanceRepository;
    }

    public Balance getBalanceByUserId(Long userId) {
        Balance balance = balanceRepository.findByUserId(userId);
        return balance;
    }

    public Balance charge(Long userId, int amount) {
        // 충전 금액이 0원 이하이면 에러
        if (amount <= 0){
            throw new BalanceException.BalaceAmountInvalid();
        }
        User user = userRepository.findById(userId);
        Balance balance = balanceRepository.findByUserId(userId);
        balance.updateAmount(amount);
        balanceRepository.save(balance);

        return new Balance(
                user,
                balance.getAmount(),
                LocalDateTime.now()
        );
    }
}
