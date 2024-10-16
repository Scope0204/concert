package hhplus.concert.api.presentation.dto.response;

import hhplus.concert.support.type.PaymentStatus;

public record PaymentResponse(
        Long paymentId,
        Long amount,
        PaymentStatus paymentStatus
) {
}
