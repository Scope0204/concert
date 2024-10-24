package hhplus.concert.application.integration;

import hhplus.concert.application.payment.dto.PaymentServiceDto;
import hhplus.concert.application.payment.usecase.PaymentFacade;
import hhplus.concert.domain.balance.models.Balance;
import hhplus.concert.domain.balance.repositories.BalanceRepository;
import hhplus.concert.domain.concert.models.Concert;
import hhplus.concert.domain.concert.models.ConcertSchedule;
import hhplus.concert.domain.concert.models.Seat;
import hhplus.concert.domain.concert.repositories.ConcertRepository;
import hhplus.concert.domain.concert.repositories.ConcertScheduleRepository;
import hhplus.concert.domain.concert.repositories.SeatRepository;
import hhplus.concert.domain.payment.models.Payment;
import hhplus.concert.domain.payment.repositories.PaymentRepository;
import hhplus.concert.domain.queue.models.Queue;
import hhplus.concert.domain.queue.repositoties.QueueRepository;
import hhplus.concert.domain.reservation.models.Reservation;
import hhplus.concert.domain.reservation.repositories.ReservationRepository;
import hhplus.concert.domain.user.models.User;
import hhplus.concert.domain.user.repositories.UserRepository;
import hhplus.concert.support.error.ErrorCode;
import hhplus.concert.support.error.exception.BusinessException;
import hhplus.concert.support.type.ConcertStatus;
import hhplus.concert.support.type.QueueStatus;
import hhplus.concert.support.type.ReservationStatus;
import hhplus.concert.support.type.SeatStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest
@Transactional
public class PaymentFacadeIntegrationTest {

    @Autowired
    private PaymentFacade paymentFacade;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private QueueRepository queueRepository;

    @Autowired
    private ConcertRepository concertRepository;

    @Autowired
    private ConcertScheduleRepository concertScheduleRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private BalanceRepository balanceRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    private User user;
    private Queue queue;
    private String token;
    private Concert concert;
    private ConcertSchedule schedule;
    private Reservation reservation;
    private Seat seat;
    private Balance balance;

    @BeforeEach
    void setUp() {
        user = new User("Test User");
        token = "test_token";
        queue = Queue.builder()
                .user(user)
                .token(token)
                .createdAt(LocalDateTime.now().minusHours(1))
                .enteredAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .status(QueueStatus.ACTIVE)
                .build();
        concert = new Concert("Test Concert", "Test Description", ConcertStatus.AVAILABLE);
        schedule = new ConcertSchedule(concert, LocalDateTime.now().plusDays(1), LocalDateTime.now().minusDays(5));
        seat = new Seat(schedule,10,10000, SeatStatus.AVAILABLE);
        reservation = new Reservation(user, concert, seat, ReservationStatus.PENDING, LocalDateTime.now());

        userRepository.save(user);
        queueRepository.save(queue);
        concertRepository.save(concert);
        concertScheduleRepository.save(schedule);
        seatRepository.save(seat);
        reservationRepository.save(reservation);
    }

    @Test
    void 존재하지않는_토큰으로_요청시_에러발생(){
        // given
        String invalidToken = "invalid Token";
        Long reservationId = 1L;

        System.out.println(user.getId());
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            paymentFacade.executePayment(user.getId(), invalidToken, reservationId);
        });
        assertEquals(ErrorCode.QUEUE_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void 대기열이_활성화되지않은_상태에서_토큰으로_API_요청시_에러발생(){
        // given
        User testUser = new User("Test User2");
        userRepository.save(testUser);

        String testToken = "expired_token";
        queueRepository.save(
                Queue.builder()
                        .user(testUser)
                        .token(testToken)
                        .createdAt(LocalDateTime.now().minusHours(1))
                        .enteredAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .status(QueueStatus.EXPIRED)
                        .build()
        );

        Long reservationId = 1L;

        // when & then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            paymentFacade.executePayment(user.getId(), testToken, reservationId);
        });
        assertEquals(ErrorCode.QUEUE_NOT_ALLOWED, exception.getErrorCode());

    }

    @Test
    void 헤더와_예약요청_userId가_일치하는지않는경우_에러발생(){
        // given
        Long otherUserId = 2L;

        // when & then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            paymentFacade.executePayment(otherUserId, token, reservation.getId());
        });
        assertEquals(ErrorCode.CLIENT_ERROR, exception.getErrorCode());
    }

    @Test
    void 잔액이_충분하지_않은경우_에러발생(){
        // given
        balance = new Balance(user, 100, LocalDateTime.now());
        balanceRepository.save(balance);

        // when & then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            paymentFacade.executePayment(user.getId(), token, reservation.getId());
        });
        assertEquals(ErrorCode.PAYMENT_INSUFFICIENT_BALANCE, exception.getErrorCode());
    }

    @Test
    void 결제가_성공적으로_완료되면_좌석상태와_토큰정보를_만료시킨다 (){
        // given
        balance = new Balance(user, 20000, LocalDateTime.now());
        balanceRepository.save(balance);
        System.out.println(reservation.getId());
        System.out.println(reservation.getStatus()); // pending

        // when
        PaymentServiceDto.Result result = paymentFacade.executePayment(user.getId(), token, reservation.getId());

        // then
        Payment payment = paymentRepository.findById(result.paymentId());

        assertNotNull(result);
        assertEquals(payment.getId(), result.paymentId());
        assertEquals(payment.getAmount(), result.amount());
        assertEquals(payment.getStatus(), result.paymentStatus());
        assertEquals(queue.getStatus(), QueueStatus.EXPIRED);
        assertEquals(reservation.getStatus(), ReservationStatus.COMPLETED);
    }
}
