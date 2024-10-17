package hhplus.concert.application.payment.usecase;

import hhplus.concert.application.payment.dto.PaymentServiceDto;
import hhplus.concert.domain.balance.components.BalanceService;
import hhplus.concert.domain.balance.models.Balance;
import hhplus.concert.domain.payment.components.PaymentService;
import hhplus.concert.domain.payment.models.Payment;
import hhplus.concert.domain.queue.components.QueueService;
import hhplus.concert.domain.queue.models.Queue;
import hhplus.concert.domain.reservation.components.ReservationService;
import hhplus.concert.domain.reservation.models.Reservation;
import hhplus.concert.domain.user.components.UserService;
import hhplus.concert.domain.user.models.User;
import hhplus.concert.support.error.exception.PaymentException;
import hhplus.concert.support.error.exception.QueueException;
import hhplus.concert.support.type.QueueStatus;
import hhplus.concert.support.type.ReservationStatus;
import org.springframework.stereotype.Service;

@Service
public class PaymentFacade {
    private final UserService userService;
    private final QueueService queueService;
    private final ReservationService reservationService;
    private final BalanceService balanceService;
    private final PaymentService paymentService;

    public PaymentFacade(UserService userService, QueueService queueService, ReservationService reservationService, BalanceService balanceService, PaymentService paymentService) {
        this.userService = userService;
        this.queueService = queueService;
        this.reservationService = reservationService;
        this.balanceService = balanceService;
        this.paymentService = paymentService;
    }

    /**
     * 결제를 진행합니다.
     * 1. 토큰이 유효한지 검증
     * 2. 헤더 userId 와 예약 userId 가 일치하는지 검증
     * 3. 결제 요청 전 잔액 확인 후 결제 처리
     * 4. 예약 좌석 상태 변경(COMPLETED)
     * 5. 대기열 상태 변경(EXPIRED)
     * 6. 실제 결제 결과를 반환합니다.
     */
    public PaymentServiceDto.Result executePayment(Long userId, String token, Long reservationId) {
        // 토큰 검증
        Queue queue = queueService.findQueueByToken(token);
        if(queue.getStatus() != QueueStatus.ACTIVE) {
            new QueueException.QueueNotFound();
        }

        // userID 검증
        User user = userService.findUserInfo(userId);
        Reservation reservation = reservationService.findById(reservationId);
        if (userId == reservation.getUser().getId()) {
            throw new PaymentException.InvalidRequest();
        }

        // 잔액이 충분한지 확인
        Balance balance = balanceService.getBalanceByUserId(userId);
        if(reservation.getSeat().getSeatPrice() > balance.getAmount()){
            throw new PaymentException.InvalidPaymentAmount();
        }

        // 결제 실행
        Payment paymentResult = paymentService.execute(user, reservation);

        // 좌석 상태 변경
        reservationService.changeStatus(reservationId, ReservationStatus.COMPLETED);

        // 대기열 상태 만료로 처리
        queueService.updateStatus(queue, QueueStatus.EXPIRED);

        return new PaymentServiceDto.Result(
                paymentResult.getId(),
                paymentResult.getAmount(),
                paymentResult.getStatus()
        );
    }
}