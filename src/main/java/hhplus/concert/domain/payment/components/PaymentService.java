package hhplus.concert.domain.payment.components;

import hhplus.concert.domain.payment.models.Payment;
import hhplus.concert.domain.payment.repositories.PaymentRepository;
import hhplus.concert.domain.reservation.models.Reservation;
import hhplus.concert.domain.user.models.User;
import hhplus.concert.support.type.PaymentStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class PaymentService {
    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    /**
     * 결제를 실행
     * 예상치 못한 예외가 발생하는 경우, 결제 상태를 실패로 저장할 수 있도록 한다.
     * @param userInfo
     * @param reservationInfo
     */
    @Transactional
    public Payment execute(User userInfo, Reservation reservationInfo) {
        try {
            Payment payment = new Payment(
                    userInfo,
                    reservationInfo,
                    reservationInfo.getSeat().getSeatPrice(),
                    PaymentStatus.COMPLETED,
                    LocalDateTime.now()
            );
            paymentRepository.save(payment);
            return payment;
        } catch (Exception e) {
            Payment failedPayment = new Payment(
                    userInfo,
                    reservationInfo,
                    reservationInfo.getSeat().getSeatPrice(),
                    PaymentStatus.FAILED,
                    LocalDateTime.now()
            );
            paymentRepository.save(failedPayment);
            return failedPayment;
        }
    }
}
