package hhplus.concert.api.presentation.response;


import hhplus.concert.application.queue.dto.QueueServiceDto;
import hhplus.concert.support.type.QueueStatus;

import java.time.LocalDateTime;

public class QueueResponse{
    public record Token(String token) {
        public static Token from(QueueServiceDto.IssuedToken issuedTokenDto) {
            return new Token(issuedTokenDto.token());
        }
    }

    public record Queue(
            QueueStatus status,
            LocalDateTime createdAt,
            int queuePosition
    ) {
        public static Queue from(QueueServiceDto.Queue queueDto) {
            return new Queue(
                    queueDto.status(),
                    queueDto.createdAt(),
                    queueDto.queuePosition()
            );
        }
    }
}
