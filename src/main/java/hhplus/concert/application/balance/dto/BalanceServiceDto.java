package hhplus.concert.application.balance.dto;

public class BalanceServiceDto {
    public record Result(
            Long userId,
            int currentAmount
    ){}
}
