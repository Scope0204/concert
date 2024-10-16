package hhplus.concert.api.presentation.dto.response;

import hhplus.concert.support.type.QueueStatus;

import java.time.LocalDateTime;

public record QueueResponse (
        Long queueId,
        QueueStatus status,
        LocalDateTime createdAt,
        int QueuePosition
){
}
