package hhplus.concert.application.balance.usecase;

import hhplus.concert.application.balance.dto.BalanceServiceDto;
import hhplus.concert.domain.balance.components.BalanceService;
import hhplus.concert.domain.balance.models.Balance;
import org.springframework.stereotype.Service;

@Service
public class BalanceFacade {

    private final BalanceService balanceService;

    public BalanceFacade(BalanceService balanceService) {
        this.balanceService = balanceService;
    }

    // 잔액 충전
    public BalanceServiceDto.Result chargeBalance(Long userId, int amount) {
        Balance balanceResult = balanceService.charge(userId, amount);

        return new BalanceServiceDto.Result(
                userId,
                balanceResult.getAmount()
        );
    }

    // 잔액 조회
    public BalanceServiceDto.Result getBalanceByUserId(Long userId) {
        Balance balanceResult = balanceService.getBalanceByUserId(userId);

        return new BalanceServiceDto.Result(
                userId,
                balanceResult.getAmount()
        );
    }

}
