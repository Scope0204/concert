package hhplus.concert.api.dto.response;

import java.time.LocalDateTime;

public record QueueTokenResponse(
        Long tokenId,
        LocalDateTime createdAt,
        LocalDateTime expiredAt
) {
}
