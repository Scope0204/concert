package hhplus.concert.application.queue.dto;

import hhplus.concert.support.type.QueueStatus;

import java.time.LocalDateTime;

public class QueueServiceDto {
    public static record IssuedToken(String token) {}

    public static record Queue(
            QueueStatus status,
            LocalDateTime createdAt,
            int queuePosition
    ) {}
}
