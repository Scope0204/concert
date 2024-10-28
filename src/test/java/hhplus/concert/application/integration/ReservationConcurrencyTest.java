package hhplus.concert.application.integration;

import hhplus.concert.application.reservation.dto.ReservationServiceDto;
import hhplus.concert.application.reservation.usecase.ReservationFacade;
import hhplus.concert.domain.concert.models.Concert;
import hhplus.concert.domain.concert.models.ConcertSchedule;
import hhplus.concert.domain.concert.models.Seat;
import hhplus.concert.domain.concert.repositories.ConcertRepository;
import hhplus.concert.domain.concert.repositories.ConcertScheduleRepository;
import hhplus.concert.domain.concert.repositories.SeatRepository;
import hhplus.concert.domain.queue.models.Queue;
import hhplus.concert.domain.queue.repositoties.QueueRepository;
import hhplus.concert.domain.reservation.repositories.ReservationRepository;
import hhplus.concert.domain.user.models.User;
import hhplus.concert.domain.user.repositories.UserRepository;
import hhplus.concert.support.type.ConcertStatus;
import hhplus.concert.support.type.QueueStatus;
import hhplus.concert.support.type.SeatStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class ReservationConcurrencyTest {
    @Autowired
    private ReservationFacade reservationFacade;

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

    @Test
    void 동시에_같은좌석을_10회_예약하는_경우_하나의_예약_요청만_성공한다() throws InterruptedException {
        // given
        List<User> users = createTestUsers(10); // 1 ~ 10명의 테스트 유저 생성
        Concert concert = createTestConcert();
        ConcertSchedule schedule = createTestConcertSchedule(concert);
        Seat seat = createTestSeat(schedule);

        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        AtomicInteger success = new AtomicInteger(0);
        AtomicInteger fail = new AtomicInteger(0);

        // when
        for (int i = 0; i < threadCount; i++) {
            final int index = i;

            executorService.submit(() -> {
                try {
                    try {
                        User user = users.get(index);
                        String token = "test_token_" + user.getId();
                        queueRepository.save(
                                Queue.builder()
                                        .user(user)
                                        .token(token)
                                        .createdAt(LocalDateTime.now().minusHours(1))
                                        .enteredAt(LocalDateTime.now())
                                        .updatedAt(LocalDateTime.now())
                                        .status(QueueStatus.ACTIVE)
                                        .build());

                        ReservationServiceDto.Request reservationRequest = new ReservationServiceDto.Request(
                                user.getId(),
                                concert.getId(),
                                schedule.getId(),
                                seat.getId()
                        );
                        reservationFacade.createReservation(reservationRequest, token);
                        success.incrementAndGet();
                    } catch (Exception e) {
                        fail.incrementAndGet();
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
        assertEquals(9, fail.get());
    }

    private List<User> createTestUsers(int count) {
        List<User> users = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            User user = new User("test_user " + i);
            userRepository.save(user);
            users.add(user);
        }
        return users;
    }

    private Concert createTestConcert() {
        Concert concert = new Concert("title","description",ConcertStatus.AVAILABLE);
        concertRepository.save(concert);
        return concert;
    }

    private ConcertSchedule createTestConcertSchedule(Concert concert) {
        ConcertSchedule concertSchedule = new ConcertSchedule(concert, LocalDateTime.now().plusDays(1), LocalDateTime.now().minusHours(1));
        concertScheduleRepository.save(concertSchedule);
        return concertSchedule;
    }

    private Seat createTestSeat(ConcertSchedule schedule) {
        Seat seat = new Seat(schedule, 1, 10000, SeatStatus.AVAILABLE);
        seatRepository.save(seat);
        return seat;
    }
}
