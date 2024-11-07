package hhplus.concert.application.payment.usecase;

import hhplus.concert.application.payment.dto.PaymentServiceDto;
import hhplus.concert.domain.balance.components.BalanceService;
import hhplus.concert.domain.balance.models.Balance;
import hhplus.concert.domain.concert.components.ConcertCacheService;
import hhplus.concert.domain.payment.components.PaymentService;
import hhplus.concert.domain.payment.models.Payment;
import hhplus.concert.domain.queue.components.QueueService;
import hhplus.concert.domain.queue.models.Queue;
import hhplus.concert.domain.reservation.components.ReservationService;
import hhplus.concert.domain.reservation.models.Reservation;
import hhplus.concert.domain.user.components.UserService;
import hhplus.concert.domain.user.models.User;
import hhplus.concert.support.error.ErrorCode;
import hhplus.concert.support.error.exception.BusinessException;
import hhplus.concert.support.type.PaymentStatus;
import hhplus.concert.support.type.QueueStatus;
import hhplus.concert.support.type.ReservationStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentFacade {
    private final UserService userService;
    private final QueueService queueService;
    private final ReservationService reservationService;
    private final BalanceService balanceService;
    private final PaymentService paymentService;
    private final ConcertCacheService concertCacheService;

    public PaymentFacade(UserService userService, QueueService queueService, ReservationService reservationService, BalanceService balanceService, PaymentService paymentService, ConcertCacheService concertCacheService) {
        this.userService = userService;
        this.queueService = queueService;
        this.concertCacheService = concertCacheService;
        this.reservationService = reservationService;
        this.balanceService = balanceService;
        this.paymentService = paymentService;
    }

    /**
     * 결제를 진행합니다.
     * 1. 헤더 userId 와 예약 userId 가 일치하는지 검증
     * 2. 토큰이 유효한지 검증
     * 3. 결제 요청 전 잔액 확인 후 결제 처리
     * 4. 예약 좌석 상태 변경(COMPLETED)
     * 5. 대기열 상태 변경(EXPIRED)
     * 6. 실제 결제 결과를 반환합니다.
     */
    @Transactional
    public PaymentServiceDto.Result executePayment(Long userId, String token, Long reservationId) {
        // userID 검증
        User user = userService.findUserInfo(userId);
        Reservation reservation = reservationService.findByIdWithPessimisticLock(reservationId);
        if (userId != reservation.getUser().getId()) {
            throw new BusinessException(ErrorCode.CLIENT_ERROR);
        }

        // 토큰 상태 검증
        Queue queue = queueService.findQueueByToken(token);
        if(queue.getStatus() != QueueStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.QUEUE_NOT_ALLOWED);
        }

        // 잔액이 충분한지 확인
        Balance balance = balanceService.getBalanceByUserId(userId);
        if(reservation.getSeat().getSeatPrice() > balance.getAmount()){
            throw new BusinessException(ErrorCode.PAYMENT_INSUFFICIENT_BALANCE);
        }

        // 결제 실행
        Payment paymentResult = paymentService.execute(user, reservation);

        // 결제가 성공적으로 완료되었을 경우, 좌석상태와 토큰 정보를 변경(아닌경우에는 그대로 유지)
        if(paymentResult.getStatus() == PaymentStatus.COMPLETED){
            // 결제 정상적으로 완료되는 경우에만 만료로 처리해야함.
            reservationService.updateStatus(reservation, ReservationStatus.COMPLETED);
            // 결제가 완료되면 캐시 상태 만료
            concertCacheService.evictConcertScheduleCache(reservation.getConcert().getId());
            // 대기열 상태 만료로 처리
            queueService.updateStatus(queue, QueueStatus.EXPIRED);
        }

        return new PaymentServiceDto.Result(
                paymentResult.getId(),
                paymentResult.getAmount(),
                paymentResult.getStatus()
        );
    }
}
