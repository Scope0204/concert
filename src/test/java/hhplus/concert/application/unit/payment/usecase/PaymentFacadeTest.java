package hhplus.concert.application.unit.payment.usecase;

import hhplus.concert.application.payment.dto.PaymentServiceDto;
import hhplus.concert.application.payment.usecase.PaymentFacade;
import hhplus.concert.domain.balance.components.BalanceService;
import hhplus.concert.domain.balance.models.Balance;
import hhplus.concert.domain.concert.models.Seat;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class PaymentFacadeTest {
    @Mock
    private UserService userService;
    @Mock
    private QueueService queueService;
    @Mock
    private ReservationService reservationService;
    @Mock
    private BalanceService balanceService;
    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private PaymentFacade paymentFacade;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private static final String TOKEN = "test_token";

    @Test
    void 토큰_검증시_만료된_대기열의_경우_예외발생(){
        // Given
        Long userId = 1L;
        Long reservationId = 1L;

        Queue queue = mock(Queue.class);

        when(queueService.findQueueByToken(TOKEN)).thenReturn(queue);
        when(queue.getStatus()).thenReturn(QueueStatus.EXPIRED); // 만료로 설정

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            paymentFacade.executePayment(userId, TOKEN, reservationId);
        });
        assertEquals(ErrorCode.QUEUE_NOT_ALLOWED, exception.getErrorCode());
    }

    @Test
    void 헤더와_유저의_ID가_일치하지않는경우_예외발생(){
        // Given
        Long userId = 1L;
        Long reservationId = 1L;

        User user = mock(User.class);
        Queue queue = mock(Queue.class);
        Reservation reservation = mock(Reservation.class);
        User invalidUser = mock(User.class);

        // 토큰 검증
        when(queueService.findQueueByToken(TOKEN)).thenReturn(queue);
        when(queue.getStatus()).thenReturn(QueueStatus.ACTIVE);

        // userID 검증
        when(userService.findUserInfo(userId)).thenReturn(user);
        when(reservationService.findById(reservationId)).thenReturn(reservation);
        when(reservation.getUser()).thenReturn(invalidUser);
        when(reservation.getUser().getId()).thenReturn(2L);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            paymentFacade.executePayment(userId, TOKEN, reservationId);
        });
        assertEquals(ErrorCode.CLIENT_ERROR, exception.getErrorCode());
    }
    @Test
    void 잔액이_충분하지않는경우_예외발생(){
        // Given
        Long userId = 1L;
        Long reservationId = 1L;

        User user = mock(User.class);
        Queue queue = mock(Queue.class);
        Reservation reservation = mock(Reservation.class);
        Seat seat = mock(Seat.class);
        Balance balance = mock(Balance.class);

        // 토큰 검증
        when(queueService.findQueueByToken(TOKEN)).thenReturn(queue);
        when(queue.getStatus()).thenReturn(QueueStatus.ACTIVE);

        // userID 검증
        when(userService.findUserInfo(userId)).thenReturn(user);
        when(reservationService.findById(reservationId)).thenReturn(reservation);
        when(reservation.getUser()).thenReturn(user);
        when(reservation.getUser().getId()).thenReturn(userId);

        // 잔액이 충분한 지 확인
        when(balanceService.getBalanceByUserId(userId)).thenReturn(balance);
        when(reservation.getSeat()).thenReturn(seat);
        when(seat.getSeatPrice()).thenReturn(1000);
        when(balance.getAmount()).thenReturn(100); // 현재 잔액을 낮게 설정

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            paymentFacade.executePayment(userId, TOKEN, reservationId);
        });
        assertEquals(ErrorCode.PAYMENT_INSUFFICIENT_BALANCE, exception.getErrorCode());
    }

    @Test
    void 결제가_성공하는_경우_좌석상태와_토큰정보를_만료처리한다(){
        // Given
        Long userId = 1L;
        Long reservationId = 1L;
        Long paymentId = 1L;

        User user = mock(User.class);
        Queue queue = mock(Queue.class);
        Reservation reservation = mock(Reservation.class);
        Seat seat = mock(Seat.class);
        Balance balance = mock(Balance.class);
        Payment payment = mock(Payment.class);

        // 토큰 검증
        when(queueService.findQueueByToken(TOKEN)).thenReturn(queue);
        when(queue.getStatus()).thenReturn(QueueStatus.ACTIVE);

        // userID 검증
        when(userService.findUserInfo(userId)).thenReturn(user);
        when(reservationService.findById(reservationId)).thenReturn(reservation);
        when(reservation.getUser()).thenReturn(user);
        when(reservation.getUser().getId()).thenReturn(userId);

        // 잔액이 충분한 지 확인
        when(balanceService.getBalanceByUserId(userId)).thenReturn(balance);
        when(reservation.getSeat()).thenReturn(seat);
        when(seat.getSeatPrice()).thenReturn(1000);
        when(balance.getAmount()).thenReturn(10000);

        // 결제 실행
        when(paymentService.execute(user,reservation)).thenReturn(payment);
        when(payment.getStatus()).thenReturn(PaymentStatus.COMPLETED); // 결제 성공 상태로 설정
        when(payment.getId()).thenReturn(paymentId);
        when(payment.getAmount()).thenReturn(1000);

        // When
        PaymentServiceDto.Result result =  paymentFacade.executePayment(userId, TOKEN, reservationId);

        // Then
        assertEquals(1L, result.paymentId());
        assertEquals(1000, result.amount());
        assertEquals(PaymentStatus.COMPLETED, result.paymentStatus());

        // 좌석 상태와 대기열 토큰 정보가 만료 처리되었는지 확인
        verify(reservationService).updateStatus(reservation, ReservationStatus.COMPLETED);
        verify(queueService).updateStatus(queue, QueueStatus.EXPIRED);
    }

    @Test
    void 결제가_실패하는_경우에는_좌석상태와_토큰정보에_변경이없다(){
        // Given
        Long userId = 1L;
        Long reservationId = 1L;
        Long paymentId = 1L;

        User user = mock(User.class);
        Queue queue = mock(Queue.class);
        Reservation reservation = mock(Reservation.class);
        Seat seat = mock(Seat.class);
        Balance balance = mock(Balance.class);
        Payment payment = mock(Payment.class);

        // 토큰 검증
        when(queueService.findQueueByToken(TOKEN)).thenReturn(queue);
        when(queue.getStatus()).thenReturn(QueueStatus.ACTIVE);

        // userID 검증
        when(userService.findUserInfo(userId)).thenReturn(user);
        when(reservationService.findById(reservationId)).thenReturn(reservation);
        when(reservation.getUser()).thenReturn(user);
        when(reservation.getUser().getId()).thenReturn(userId);

        // 잔액이 충분한 지 확인
        when(balanceService.getBalanceByUserId(userId)).thenReturn(balance);
        when(reservation.getSeat()).thenReturn(seat);
        when(seat.getSeatPrice()).thenReturn(1000);
        when(balance.getAmount()).thenReturn(10000);

        // 결제 실행
        when(paymentService.execute(user,reservation)).thenReturn(payment);
        when(payment.getStatus()).thenReturn(PaymentStatus.FAILED); // 결제 실패 상태로 설정
        when(payment.getId()).thenReturn(paymentId);
        when(payment.getAmount()).thenReturn(1000);

        // When
        PaymentServiceDto.Result result =  paymentFacade.executePayment(userId, TOKEN, reservationId);

        // Then
        assertEquals(1L, result.paymentId());
        assertEquals(1000, result.amount());
        assertEquals(PaymentStatus.FAILED, result.paymentStatus());

        // 결제가 실패했으므로 좌석 상태와 대기열 상태는 변경되지 않음
        verify(reservationService, never()).changeStatus(anyLong(), any(ReservationStatus.class));
        verify(queueService, never()).updateStatus(any(Queue.class), any(QueueStatus.class));
    }

}