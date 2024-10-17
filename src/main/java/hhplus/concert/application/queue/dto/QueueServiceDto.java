package hhplus.concert.application.queue.dto;

import hhplus.concert.support.type.QueueStatus;

import java.time.LocalDateTime;

public class QueueServiceDto {
    public record IssuedToken(String token) {}

    public record Queue(
            QueueStatus status,
            LocalDateTime createdAt,
            int queuePosition
    ) {}
}
