package hhplus.concert.domain.payment.components;

import hhplus.concert.domain.payment.event.components.PaymentEventPublisher;
import hhplus.concert.domain.payment.event.models.PaymentEvent;
import hhplus.concert.domain.payment.models.Payment;
import hhplus.concert.domain.payment.repositories.PaymentRepository;
import hhplus.concert.domain.reservation.models.Reservation;
import hhplus.concert.domain.user.models.User;
import hhplus.concert.support.type.PaymentStatus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final PaymentEventPublisher paymentEventPublisher;

    public PaymentService(PaymentRepository paymentRepository, @Qualifier("applicationPublisher") PaymentEventPublisher paymentEventPublisher) {
        this.paymentRepository = paymentRepository;
        this.paymentEventPublisher = paymentEventPublisher;
    }

    /**
     * 결제를 실행
     * 예상치 못한 예외가 발생하는 경우, 결제 상태를 실패로 저장할 수 있도록 한다.
     * 결제가 완료되면, 이벤트 퍼블리셔를 통해 이벤트를 발행하여 비동기 작업을 처리할 수 있도록 한다.
     * @param userInfo
     * @param reservationInfo
     */
    public Payment execute(User userInfo, Reservation reservationInfo) {
        Payment payment;

        try {
            payment = new Payment(
                    userInfo,
                    reservationInfo,
                    reservationInfo.getSeat().getSeatPrice(),
                    PaymentStatus.COMPLETED,
                    LocalDateTime.now()
            );
        } catch (Exception e) {
            payment = new Payment(
                    userInfo,
                    reservationInfo,
                    reservationInfo.getSeat().getSeatPrice(),
                    PaymentStatus.FAILED,
                    LocalDateTime.now()
            );
        }

        paymentRepository.save(payment);
        paymentEventPublisher.publishPaymentEvent(new PaymentEvent(payment.getId()));

        return payment;
    }
}
