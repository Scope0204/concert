package hhplus.concert.api.dto.response;

import hhplus.concert.common.type.PaymentStatus;

public record PaymentResponse(
        Long paymentId,
        Long amount,
        PaymentStatus paymentStatus
) {
}
