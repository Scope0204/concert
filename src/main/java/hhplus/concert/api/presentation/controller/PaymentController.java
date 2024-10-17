package hhplus.concert.api.presentation.controller;

import hhplus.concert.api.presentation.request.PaymentRequest;
import hhplus.concert.api.presentation.response.PaymentResponse;
import hhplus.concert.application.payment.usecase.PaymentFacade;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payment")
public class PaymentController {

    private final PaymentFacade paymentFacade;

    public PaymentController(PaymentFacade paymentFacade) {
        this.paymentFacade = paymentFacade;
    }

    // 콘서트 좌석 예약에 대한 결제를 진행합니다.
    @PostMapping("/concerts/users")
    public PaymentResponse.Result executePayment(
            @RequestHeader("User-Id") Long userId,
            @RequestHeader("Authorization") String token,
            @RequestBody PaymentRequest.Detail paymentRequest) {
        return PaymentResponse.Result.from(paymentFacade.executePayment(
                userId,
                token,
                paymentRequest.reservationId()));
    }
}
