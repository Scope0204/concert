package hhplus.concert.application.payment.dto;

import hhplus.concert.support.type.PaymentStatus;

public class PaymentServiceDto {
    public record Result(
            Long paymentId,
            int amount,
            PaymentStatus paymentStatus
    ){}
}
