package hhplus.concert.api.interfaces.response;


import hhplus.concert.application.queue.dto.QueueServiceDto;
import hhplus.concert.support.type.QueueStatus;

public class QueueResponse{
    public record Token(String token) {
        public static Token from(QueueServiceDto.IssuedToken issuedTokenDto) {
            return new Token(issuedTokenDto.token());
        }
    }

    public record Queue(
            QueueStatus status,
            Long queuePosition
    ) {
        public static Queue from(QueueServiceDto.Queue queueDto) {
            return new Queue(
                    queueDto.status(),
                    queueDto.queuePosition()
            );
        }
    }
}
