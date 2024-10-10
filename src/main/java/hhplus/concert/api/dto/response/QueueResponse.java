package hhplus.concert.api.dto.response;

import hhplus.concert.common.type.QueueStatus;

import java.time.LocalDateTime;

public record QueueResponse (
        Long queueId,
        QueueStatus status,
        LocalDateTime createdAt
){
}
