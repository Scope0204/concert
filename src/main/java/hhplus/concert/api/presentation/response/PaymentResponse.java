package hhplus.concert.api.presentation.response;

import hhplus.concert.application.payment.dto.PaymentServiceDto;
import hhplus.concert.support.type.PaymentStatus;

public class PaymentResponse {
    public record Result(
            Long paymentId,
            int amount,
            PaymentStatus paymentStatus
    ){
        public static Result from(PaymentServiceDto.Result resultDto){
            return new Result(
                    resultDto.paymentId(),
                    resultDto.amount(),
                    resultDto.paymentStatus()
            );
        }
    }
}
