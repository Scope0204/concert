package hhplus.concert.api.dto.response;

public record BalanceResponse(
        Long userId,
        Long currentAmount
) {
}
