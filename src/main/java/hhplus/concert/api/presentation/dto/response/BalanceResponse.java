package hhplus.concert.api.presentation.dto.response;

public record BalanceResponse(
        Long userId,
        Long currentAmount
) {
}
