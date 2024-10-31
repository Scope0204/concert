package hhplus.concert.application.balance.usecase;

import hhplus.concert.application.balance.dto.BalanceServiceDto;
import hhplus.concert.domain.balance.components.BalanceService;
import hhplus.concert.domain.balance.models.Balance;
import hhplus.concert.support.annotation.DistributedLock;
import hhplus.concert.support.error.ErrorCode;
import hhplus.concert.support.error.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BalanceFacade {

    private final BalanceService balanceService;

    public BalanceFacade(BalanceService balanceService) {
        this.balanceService = balanceService;
    }

    // 잔액 충전
    @DistributedLock(key = "#userId")
    public BalanceServiceDto.Result chargeBalance(Long userId, int amount) {
        // 충전 금액이 0원 이하이면 에러
        if (amount <= 0){
            throw new BusinessException(ErrorCode.BALANCE_INVALID_CHARGE_AMOUNT);
        }
        Balance balanceResult = balanceService.charge(userId, amount);

        return new BalanceServiceDto.Result(
                balanceResult.getUser().getId(),
                balanceResult.getAmount()
        );
    }

    // 잔액 조회
    @Transactional
    public BalanceServiceDto.Result getBalanceByUserId(Long userId) {
        Balance balanceResult = balanceService.getBalanceByUserId(userId);

        return new BalanceServiceDto.Result(
                balanceResult.getUser().getId(),
                balanceResult.getAmount()
        );
    }
}
