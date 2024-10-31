package hhplus.concert.domain.balance.components;

import hhplus.concert.domain.balance.models.Balance;
import hhplus.concert.domain.balance.repositories.BalanceRepository;
import hhplus.concert.domain.user.repositories.UserRepository;
import hhplus.concert.support.error.ErrorCode;
import hhplus.concert.support.error.exception.BusinessException;
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
        if(balance == null ) {
            throw new BusinessException(ErrorCode.BALANCE_NOT_FOUND);
        }
        return balance;
    }

    /**
     * 잔액 충전 요청
     * 헤당 유저의 잔액 정보가 없는 걍우 새롭게 생성
     * 기존 잔액이 있는 경우 금액 업데이트
     * @param userId
     * @param amount
     * @return
     */
    public Balance charge(Long userId, int amount) {
        Balance balance = balanceRepository.findByUserId(userId);
        //Balance balance = balanceRepository.findByUserIdWithPessimisticLock(userId); // 비관적 락을 적용

        if (balance == null) {
            balance = new Balance(userRepository.findById(userId), amount, LocalDateTime.now());
        } else {
            balance.updateAmount(amount);
        }

        balanceRepository.save(balance);

        return new Balance(
                balance.getUser(),
                balance.getAmount(),
                balance.getUpdatedAt()
        );
    }
}
