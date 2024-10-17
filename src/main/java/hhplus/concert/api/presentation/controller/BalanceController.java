package hhplus.concert.api.presentation.controller;

import hhplus.concert.api.presentation.request.BalanceRequest;
import hhplus.concert.api.presentation.response.BalanceResponse;
import hhplus.concert.application.balance.usecase.BalanceFacade;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/balance")
public class BalanceController {

    private final BalanceFacade balanceFacade;

    public BalanceController(BalanceFacade balanceFacade) {
        this.balanceFacade = balanceFacade;
    }

    // 잔액을 충전한다.
    @PostMapping("/users/{userId}/charge")
    public BalanceResponse.Result chargeBalance(
            @RequestHeader("User-Id") Long userId,
            BalanceRequest.Charge balanceRequest
    ){
        return BalanceResponse.Result.from(balanceFacade.chargeBalance(userId, balanceRequest.amount()));
    }

    // 잔액을 조회한다.
    @GetMapping("/users/{userId}")
    public BalanceResponse.Result getUserBalance(
            @RequestHeader("User-Id") Long userId
    ){
        return BalanceResponse.Result.from(balanceFacade.getBalanceByUserId(userId));
    }
}
