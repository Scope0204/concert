package hhplus.concert.api.interfaces.controller;

import hhplus.concert.api.interfaces.request.PaymentRequest;
import hhplus.concert.api.interfaces.response.PaymentResponse;
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
            @RequestHeader("Token") String token,
            @RequestBody PaymentRequest.Detail paymentRequest) {
        return PaymentResponse.Result.from(paymentFacade.executePayment(
                userId,
                token,
                paymentRequest.reservationId()));
    }
}
