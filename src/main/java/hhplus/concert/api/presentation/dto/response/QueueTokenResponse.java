package hhplus.concert.api.presentation.dto.response;

import java.time.LocalDateTime;

public record QueueTokenResponse(
        String tokenId,
        LocalDateTime createdAt
) {
}
