package hhplus.concert.application.queue.dto;

import hhplus.concert.support.type.QueueStatus;

public class QueueServiceDto {
    public record IssuedToken(String token) {}

    public record Queue(
            QueueStatus status,
            Long queuePosition
    ) {}
}
