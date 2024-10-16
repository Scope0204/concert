package hhplus.concert.api.presentation.response;

import hhplus.concert.application.queue.dto.QueueServiceDto;
import hhplus.concert.support.type.QueueStatus;

public class QueueResponse{
    public static record Token(String token) {
        public static Token from(QueueServiceDto.IssuedToken issuedTokenDto) {
            return new Token(issuedTokenDto.token());
        }
    }

    public static record Queue(QueueStatus status, long remainingWaitListCount, long estimatedWaitTime) {
        public static Queue from(QueueServiceDto.Queue queueDto) {
            return new Queue(
                    queueDto.status(),
                    queueDto.remainingWaitListCount(),
                    queueDto.estimatedWaitTime()
            );
        }
    }
}
