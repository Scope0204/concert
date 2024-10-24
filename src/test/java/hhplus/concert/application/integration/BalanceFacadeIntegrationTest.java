package hhplus.concert.application.integration;

import hhplus.concert.application.balance.dto.BalanceServiceDto;
import hhplus.concert.application.balance.usecase.BalanceFacade;
import hhplus.concert.domain.balance.models.Balance;
import hhplus.concert.domain.balance.repositories.BalanceRepository;
import hhplus.concert.support.error.ErrorCode;
import hhplus.concert.support.error.exception.BusinessException;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class BalanceFacadeIntegrationTest {

    @Autowired
    private BalanceFacade balanceFacade;

    @Autowired
    private BalanceRepository balanceRepository;


    /**
     * data.sql 참고. 유저 별로 잔액 충전한 상태
     */
    @Test
    void 유효한_유저의_잔액_조회를_성공 (){
        // given
        Long userId = 1L;

        // when
        BalanceServiceDto.Result result = balanceFacade.getBalanceByUserId(userId);

        // then
        Balance balance = balanceRepository.findByUserId(userId);
        assertNotNull(result);
        assertEquals(balance.getId(), result.userId());
        assertEquals(balance.getAmount(), result.currentAmount());
    }

    @Test
    void 유효한_유저의_잔액을_충전 (){
        // given
        Long userId = 1L;
        Balance balance = balanceRepository.findByUserId(userId);
        int baseAmount = balance.getAmount();
        int plusAmount = 20000;

        // when
        BalanceServiceDto.Result result = balanceFacade.chargeBalance(userId, plusAmount);

        // then
        assertNotNull(result);
        assertEquals(userId, result.userId());
        assertEquals(baseAmount+plusAmount, result.currentAmount());
    }

    @Test
    void 존재하지않는_유저정보로_조회하는_경우_에러발생 () {
        // given
        Long userId = 999L;

        // when & then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            balanceFacade.getBalanceByUserId(userId);
        });
        assertEquals(ErrorCode.BALANCE_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void 유저는_존재하나_충전금액이_0원_이하인_경우_에러발생 () {
        // given
        Long userId = 1L;
        int amount = 0;

        // when & then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            balanceFacade.chargeBalance(userId,amount);
        });
        assertEquals(ErrorCode.BALANCE_INVALID_CHARGE_AMOUNT, exception.getErrorCode());
    }

}
