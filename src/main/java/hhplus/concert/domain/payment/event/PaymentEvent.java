package hhplus.concert.domain.payment.event;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class PaymentEvent {
    private Long paymentId;
}
