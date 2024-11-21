package hhplus.concert.api.interfaces.response;

import hhplus.concert.application.balance.dto.BalanceServiceDto;

public class BalanceResponse {
    public record Result(
            Long userId,
            int currentAmount
    ){
        public static Result from(BalanceServiceDto.Result resultDto){
            return new Result(
                    resultDto.userId(),
                    resultDto.currentAmount()
            );
        }
    }
}
