package hhplus.concert.application.unit.balance.usecase;

import hhplus.concert.domain.balance.components.BalanceService;
import hhplus.concert.domain.balance.models.Balance;
import hhplus.concert.domain.balance.repositories.BalanceRepository;
import hhplus.concert.domain.user.models.User;
import hhplus.concert.domain.user.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class BalanceFacadeTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private BalanceRepository balanceRepository;

    @InjectMocks
    private BalanceService balanceService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void 잔액이_존재하는_사용자의_잔액_충전_테스트() {
        // Given
        Long userId = 0L;
        int baseAmount = 100;
        int chargeAmount = 300;

        User user = new User("jkcho");
        Balance balance = new Balance(
                user,
                baseAmount,
                LocalDateTime.now()
        );

        when(balanceRepository.findByUserId(userId)).thenReturn(balance);

        // When
        Balance result = balanceService.charge(userId, chargeAmount);

        // Then
        assertThat(baseAmount + chargeAmount).isEqualTo(result.getAmount());
    }

    @Test
    void 잔액정보가_존재하지않는_사용자의_잔액_충전_테스트() {
        // Given
        Long userId = 0L;
        int chargeAmount = 300;

        User user = new User("jkcho");

        when(balanceRepository.findByUserId(userId)).thenReturn(null);
        when(userRepository.findById(userId)).thenReturn(user);

        // When
        Balance result = balanceService.charge(userId, chargeAmount);

        // Then
        assertThat(chargeAmount).isEqualTo(result.getAmount());
    }

    @Test
    void 잔액_조회_테스트() {
        // Given
        Long userId = 0L;
        int baseAmount = 100;

        User user = new User("jkcho");
        Balance balance = new Balance(
                user,
                baseAmount,
                LocalDateTime.now()
        );

        when(userRepository.findById(userId)).thenReturn(user);
        when(balanceRepository.findByUserId(userId)).thenReturn(balance);

        // When
        Balance result = balanceService.getBalanceByUserId(userId);

        // Then
        assertThat(baseAmount).isEqualTo(result.getAmount());
    }
}