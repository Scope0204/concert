package hhplus.concert.application.integration;

import hhplus.concert.application.payment.usecase.PaymentFacade;
import hhplus.concert.domain.balance.models.Balance;
import hhplus.concert.domain.balance.repositories.BalanceRepository;
import hhplus.concert.domain.concert.models.Concert;
import hhplus.concert.domain.concert.models.ConcertSchedule;
import hhplus.concert.domain.concert.models.Seat;
import hhplus.concert.domain.concert.repositories.ConcertRepository;
import hhplus.concert.domain.concert.repositories.ConcertScheduleRepository;
import hhplus.concert.domain.concert.repositories.SeatRepository;
import hhplus.concert.domain.payment.repositories.PaymentRepository;
import hhplus.concert.domain.queue.models.Queue;
import hhplus.concert.domain.queue.repositoties.QueueRepository;
import hhplus.concert.domain.reservation.models.Reservation;
import hhplus.concert.domain.reservation.repositories.ReservationRepository;
import hhplus.concert.domain.user.models.User;
import hhplus.concert.domain.user.repositories.UserRepository;
import hhplus.concert.support.type.ConcertStatus;
import hhplus.concert.support.type.QueueStatus;
import hhplus.concert.support.type.ReservationStatus;
import hhplus.concert.support.type.SeatStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class PaymentConcurrencyTest {
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
        balance = new Balance(user, 100000, LocalDateTime.now());

        userRepository.save(user);
        queueRepository.save(queue);
        concertRepository.save(concert);
        concertScheduleRepository.save(schedule);
        seatRepository.save(seat);
        reservationRepository.save(reservation);
        balanceRepository.save(balance);
    }

    @Test
    void 동시에_10번_결제를_요청하면_1번만_성공한다 () throws InterruptedException {
        // given
        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        AtomicInteger success = new AtomicInteger(0);
        AtomicInteger failed = new AtomicInteger(0);

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    try {
                        System.out.println(Thread.currentThread().getName() + " - Start payment");
                        paymentFacade.executePayment(user.getId(), token, reservation.getId());
                        success.incrementAndGet();
                        System.out.println(Thread.currentThread().getName() + " - Payment success");

                    } catch (Exception e) {
                        failed.incrementAndGet();
                        System.out.println(Thread.currentThread().getName() + " - Payment failed: " + e.getMessage());
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        executorService.shutdown();

        // then
        assertEquals(1, success.get());
        assertEquals(9, failed.get());
    }

}
