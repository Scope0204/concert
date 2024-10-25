package hhplus.concert.application.integration;

import hhplus.concert.application.balance.usecase.BalanceFacade;
import hhplus.concert.domain.balance.models.Balance;
import hhplus.concert.domain.balance.repositories.BalanceRepository;
import hhplus.concert.domain.user.models.User;
import hhplus.concert.domain.user.repositories.UserRepository;
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
public class BalanceConcurrencyTest {

    @Autowired
    private BalanceFacade balanceFacade;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BalanceRepository balanceRepository;

    @Test
    void 동시에_5번_충전_요청을_하는경우_1번만_성공한다 () throws InterruptedException {
        // given
        User user = new User("scope");
        userRepository.save(user);

        Balance balance = new Balance(user, 1000, LocalDateTime.now());
        balanceRepository.save(balance);

        int threadCount = 5;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        int chargeAmount = 100;

        AtomicInteger successfulCharges = new AtomicInteger(0);
        AtomicInteger failedCharges = new AtomicInteger(0);

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    try {
                        balanceFacade.chargeBalance(user.getId(), chargeAmount);
                        successfulCharges.incrementAndGet();
                    } catch (Exception e) {
                        failedCharges.incrementAndGet();
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        executorService.shutdown();

        // then
        Balance finalBalance = balanceRepository.findByUserId(user.getId());
        assertEquals(1, successfulCharges.get());
        assertEquals(4, failedCharges.get());
        assertEquals(1100, finalBalance.getAmount());
    }
}
