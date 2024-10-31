# 동시성 이슈 파악 및 대응 방법

### 1. 콘서트 예약 서비스 동시성 발생 가능 이슈

#### 1) 좌석 예약

- 비즈니스 요구 사항
  - 여러 사용자가 하나의 좌석을 두고 동시에 예약하려고 하는 경우, 단 1명만 해당 좌석을 예약할 수 있도록 합니다.
- 관련 동시성 이슈
  - 시스템이 각 좌석 예약에 대한 요청을 순차적으로 처리하지 않으면 중복 예약이 발생할 수 있습니다.
  - 데이터베이스 트랜잭션이 적절히 관리되지 않는 경우, 한 사용자의 예약 과정 중에 다른 사용자 예약 요청이 들어와 처리될 수 있습니다.

#### 2) 잔액 충전

- 비즈니스 요구 사항
  - 사용자가 자신의 잔액을 충전하려고 할 때, 실수로 여러번 충전 요청을 하는 경우 단 한번만 가능하도록 합니다.
- 관련 동시성 이슈
  - 사용자의 실수로 인해 충전 버튼을 여러번 누르는 경우, 각 요청을 독립적으로 처리하는 과정에서 중복 충전이 될 수 있습니다.
  - 서버에서 충전 요청을 처리하는 동안 클라이언트로 충전 요청이 들어오는 경우, 두 스레드가 동일한 잔액에 접근하는 경합상태에 빠질 수 있습니다.

### 2. 동시성 이슈 대응 전 초기 비즈니스 로직 분석

비즈니스 서비스 로직에(Facade) `@Transactional` 을 적용하여 관리하고 있습니다.

- 원자성: 모든 데이터베이스 연산이 하나의 트랜잭션으로 관리되도록 하였습니다.
- 일관성: 관련 비즈니스 로직이 Facade안에서 하나의 트랜잭션 내에서 실행되므로, 데이터의 일관성을 유지하기 쉽도록 하였습니다.
- 단점: Facade로 여러 작업들이 하나의 트랜잭션으로 관리되어지고 있기 때문에,
  대기 시간이 증가하여 동시 처리 성능이 떨어질 수 있습니다.

#### 1) 좌석 예약

```java
@Service
public class ReservationFacade {

...

@Transactional
public ReservationServiceDto.Result createReservation(ReservationServiceDto.Request reservationRequest, String token) {

        validateQueueStatus(token);

        Reservation reservation = reservationService.createReservation(
                reservationRequest.userId(),
                reservationRequest.concertId(),
                reservationRequest.concertScheduleId(),
                reservationRequest.seatId()
        );

        return new ReservationServiceDto.Result(
                reservation.getId(),
                reservation.getConcert().getId(),
                reservation.getConcert().getTitle(),
                reservation.getSeat().getConcertSchedule().getConcertAt(),
                reservation.getSeat().getSeatNumber(),
                reservation.getSeat().getSeatPrice(),
                reservation.getStatus()
        );
    }

```

```java
@Service
public class ReservationService {

...

public Reservation createReservation(Long userId, Long concertId, Long concertScheduleId, Long seatId) {

        User user = userRepository.findById(userId);
        Concert concert = concertRepository.findById(concertId);
        Seat seat = seatRepository.findById(seatId);
        if(seat.getStatus() == SeatStatus.UNAVAILABLE) {
            throw new BusinessException(ErrorCode.CONCERT_SEAT_NOT_AVAILABLE);
        }

        Reservation reservation = new Reservation(user, concert, seat,  ReservationStatus.PENDING,
LocalDateTime.now());
        reservationRepository.save(reservation);

        seat.updateStatus(SeatStatus.UNAVAILABLE);
        seatRepository.save(seat);

        return reservation;
    }
```

(가독성을 위해 비즈니스 로직 외에는 생략하도록 하였습니다.)

1. `ReservationFacade`

- 좌석 예약 요청 `createReservation` 메서드를 정의.
- 예약 요청의 유효성을 검사한 후(토큰 검증), `ReservationService`를 통해 실제 예약 요청을 생성합니다.

2. `ReservationService`

- 실제 예약 요청 생성과 좌석 상태 업데이트를 수행합니다.
- 사용자, 콘서트, 스케줄, 좌석 정보를 조회합니다.
- `Reservation` 객체를 생성하고 저장합니다.
- 예약이 완료된 좌석 상태를 `UNAVAILABLE`로 예약 불가능한 상태로 업데이트합니다.

#### 2) 잔액 충전 `chargeBalance`

`BalanceFacade` 의 잔액 충전 요청 메서드

```java
@Service
public class BalanceFacade {

...

@Transactional
public BalanceServiceDto.Result chargeBalance(Long userId, int amount) {
        if (amount <= 0){
            throw new BusinessException(ErrorCode.BALANCE_INVALID_CHARGE_AMOUNT);
        }
        Balance balanceResult = balanceService.charge(userId, amount);

        return new BalanceServiceDto.Result(balanceResult.getUser().getId(),balanceResult.getAmount());
    }
```

```java
@Service
public class BalanceService {

...

public Balance charge(Long userId, int amount) {

        Balance balance = balanceRepository.findByUserId(userId);

        if (balance == null) {
            balance = new Balance(userRepository.findById(userId), amount, LocalDateTime.now());
        } else {
            balance.updateAmount(amount);
        }

        balance.updateAmount(amount);
        balanceRepository.save(balance);

        return new Balance(balance.getUser(), balance.getAmount(), balance.getUpdatedAt());
    }
```

1. `BalanceFacade`

   - 충전 금액 유효성을 검사한 후, BalanceService를 통해 실제 잔액 충전 로직을 수행합니다.

2. `BalanceService`

   - `updateAmount` 메서드에서 사용자의 잔액을 업데이트 합니다.
   - 해당 사용자의 Balance 엔티티가 존재하면 충전 금액만큼 업데이트하고, 없으면 새로 생성합니다.

### 3. DB Lock 을 이용한 동시성 제어

### 1) 낙관적 락

#### 특징

- 낙관적 락은 데이터 충돌 가능성이 낮은 경우에 주로 사용합니다.
- 데이터를 읽을 때 충돌이 발생하지 않을 것이라고 '낙관적으로' 가정하고 데이터베이스에 락을 걸지 않고, 수정할 때만 충돌을 감지하여 처리하는 방식입니다.

#### 장점

- 트랜잭션, Lock 설정 없이 데이터 정합성을 보장할 수 있으므로 성능적으로 우위를 가질 수 있습니다.

#### 단점

- 동시에 너무 많은 요청이 들어와 실패가 자주 일어나면 데이터 정합성을 만족하지 못할 수 있습니다.

#### 실제 로직에 적용(잔액 충전)

```java
@Getter
@NoArgsConstructor
@Entity
public class Balance {

    @Version
    private Long version;

}
```

- `Balance` 엔티티에 `@Version` 어노테이션을 사용한 version 이라는 필드 값을 추가하여 JPA의 낙관적 락 기능을 활용하도록 합니다.
- 해당 필드 값은 JPA에 의해 자동으로 관리되며, 엔티티가 업데이트될 때마다 증가됩니다.
- 엔티티를 업데이트할 때, JPA는 현재 데이터베이스에 저장된 버전 번호와 비교합니다.
- 엔티티의 버전 번호가 데이터베이스에 저장된 버전과 일치하지 않으면, `OptimisticLockException` 예외가 발생합니다.

#### 잔액 충전 - 테스트 코드 작성(성공 케이스)

```java
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
```

- 잔액 충전에 대한 동시성 이슈를 확인하는 테스트 코드입니다.
- 5개의 스레드를 생성하여, 동시에 잔액 충전을 시도합니다.
- 각 스레드는 100원씩 충전을 시도합니다.
- 테스트 결과, 한번의 요청만 성공되었음을 확인할 수 있었습니다.

#### 잔액 충전 - 테스트 코드 작성(실패 케이스)

위 테스트 케이스에서 스레드 수를 100개로 늘려보도록 하였습니다.

```java
int threadCount = 100;
```

- 테스트 결과, 한번이 아닌 여러번의 요청이 성공되며 테스트가 실패하는 것을 확인할 수 있었습니다.

#### 기술에 대한 고찰

1. 낙관적 락의 동시성 제어 및 한계 확인

   - 5회 정도의 적은 동시 요청에 대해서는 낙관적 락이 효과적으로 동작함을 확인할 수 있었습니다.

2. 동시성 증가에 따른 한계 확인

   - 하지만 잔액 충전의 경우, 100회 정도의 많은 동시 요청 테스트에서는 실패가 발생했습니다.
   - 동시 요청 수가 증가함 에따라 충돌 빈도가 많이 일어남을 알 수 있었습니다.
   - 충돌이 발생할 때마다 예외가 발생하고 재시도가 필요하므로, 로직이 복잡한경우에는 전체적인 처리 시간이 오히려 길어질 수 있습니다.
   - 따라서 동시 요청이 많은 환경에서는 낙관적 락은 적합하지 않음을 알 수 있었습니다.

3. 효율성

   - 트랜잭션, 락 설정 없이 동작하므로 높은 성능을 확인할 수 있었습니다.(테스트 실행 시간이 짧음)
   - 낙관적 락은 충돌이 적은 환경에서는 높은 성능을 제공함을 알 수 있었습니다.

4. 재시도 로직 구현의 필요성
   - 낙관적 락 실패 시 재시도 로직을 구현하면 일시적인 충돌로 인한 실패를 줄이고 성공률을 높일 수 있을 것 같습니다.
   - 하지만 충돌이 너무 빈번하게 일어나게되면 오히려 재시도 로직에 의한 시스템의 부하가 더 커질 수도 있을 것 같습니다.

결론적으로, 낙관적 락은 간단하고 효과적인 동시성 제어 방법이지만, 높은 동시성 환경에서는 한계가 있음을 확인하였습니다.

### 2) 비관적 락

#### 특징

- 비관적 락은 동시 충돌이 빈번하게 일어나는 경우에 주로 사용합니다.
- 비관적 락은 동시 충돌이 빈번하게 일어날 수 있음을 '비관적으로' 가정하고, 데이터를 읽는 시점에 락을 걸어 다른 트랜잭션의 접근을 차단하도록 합니다.

#### 장점

- 트랜잭션이 시작되면 해당 데이터에 다른 트랜잭션이 접근하지 못하도록 락을 겁니다. 이를 통해 데이터 일관성을 보장합니다
- 데이터 정합성을 강하게 보장할 수 있는 방식입니다.

#### 단점

- 단, 락이 걸리는 동안 다른 트랜잭션은 해당 데이터에 접근할 수 없어, 대기 시간이 증가하고 전체적인 성능이 저하될 수 있습니다.

#### 실제 로직에 적용(좌석 예약)

```java
@Service
public class ReservationService {

...

public Reservation createReservation(Long userId, Long concertId, Long concertScheduleId, Long seatId) {

        User user = userRepository.findById(userId);
        Concert concert = concertRepository.findById(concertId);
        Seat seat = seatRepository.findByIdWithPessimisticLock(seatId); // 비관적 락을 적용
        if(seat.getStatus() == SeatStatus.UNAVAILABLE) {
            throw new BusinessException(ErrorCode.CONCERT_SEAT_NOT_AVAILABLE);
        }

        Reservation reservation = new Reservation(user, concert, seat,  ReservationStatus.PENDING,
LocalDateTime.now());
        reservationRepository.save(reservation);

        seat.updateStatus(SeatStatus.UNAVAILABLE);
        seatRepository.save(seat);

        return reservation;
    }
```

```java
public interface JpaSeatRepository extends JpaRepository<Seat, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT seat FROM Seat seat WHERE seat.id = :seatId")
    Optional<Seat> findByIdWithPessimisticLock(@Param("seatId") Long seatId);
}
```

- `ReservationService`
  - `findAllByIdWithPessimisticLock` 메서드를 사용하여 비관적 락이 걸린 상태로 공유 자원인 `Seat` 엔티티를 조회하도록 합니다.
- `JpaSeatRepository`
  - `@Lock(LockModeType.PESSIMISTIC_WRITE)` 어노테이션을 사용하여 비관적 락을 구현하였습니다.
  - `PESSIMISTIC_WRITE`는 해당 데이터에 **배타적**으로 접근하도록 합니다. 이는 다른 트랜잭션이 해당 자원에 대한 읽기와 쓰기 모두 불가능하게 되어 충돌을 방지합니다.
  - `PESSIMISTIC_READ`로 변경하여 읽기 락(데이터에 대해 동시 읽기 접근을 허용하면서 쓰기 접근을 막음으로 충돌을 방지함)으로 구현하게 되면 조금의 성능개선이 있을 수도 있을 것 같습니다.
  - 좌석 예약 서비스의 경우 공유자원(`seat`)에 대한 쓰기 접근에 대한 방지만 해주어도 어느정도 데이터 무결성이 유지될 것 같습니다.

#### 테스트 코드 작성(성공 케이스)

```java
@Test
void 동시에_같은좌석을_여러명이_예약하는_경우_하나의_예약_요청만_성공한다() throws InterruptedException {

    // given
    int threadCount = 1000;

    List<User> users = createTestUsers(threadCount); // threadCount 만큼의 테스트 유저 생성
    Concert concert = createTestConcert();
    ConcertSchedule schedule = createTestConcertSchedule(concert);
    Seat seat = createTestSeat(schedule);

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
    assertEquals(999, fail.get());
    Seat updatedSeat = seatRepository.findById(seat.getId());
    assertEquals(SeatStatus.UNAVAILABLE, updatedSeat.getStatus()); // 좌석 상태 확인
}
```

- 좌석 결제에 대한 동시성 이슈를 확인하는 테스트 케이스 입니다.
- 1000개의 스레드와 1000명의 유저를 생성하여, 동시에 동일 좌석에 대한 예약을 시도합니다.
- 비즈니스 로직 상, 예약 요청이 이루어진 좌석의 경우 다시 예약할 수 없도록 상태를 업데이트 합니다.
- 따라서, 단 한명만 좌석을 예약할 수 있고 나머지 요청들은 좌석 요청을 실패하고 예외를 반환하게 됩니다.
- 테스트 결과, 한명의 요청만 성공되었음을 확인할 수 있었습니다
- 해당 좌석상태도 `UNAVAILABLE`로 변경됨을 확인할 수 있었습니다.

#### 기술에 대한 고찰

1. 비관적 락의 동시성 제어 확인
   - 확실히 비관적 락의 경우, 많은 요청이 동시에 일어나는 환경에서도 데이터 무결성을 보장해주는 것을 확인 할 수 있었습니다,
2. 효율성
   - 동시에 처리할 수 있는 트랜잭션의 수가 제한되므로 전체적인 시스템 처리량이 낮아질 수도 있을 것 같습니다.
   - 실제로 테스트 시간도 낙관적 락에 비해 오래 걸립니다.

결론적으로, 비관적 락은 충돌이 자주 발생하는 환경에서도 효과적인 동시성 제어를 할 수 있음을 확인할 수 있었습니다.
충돌이 자주 발생하거나 데이터 정합성이 중요한 공유자원을 관리하는데에는 낙관적 락보다 유용하다는 것을 알 수 있었습니다.
다만 시스템 속도는 낙관적 락에 비해 조금 떨어집니다.

#### DB Lock 방식에 대한 결론

비즈니스 요구 사항 및 실제 서비스의 동시 요청 패턴과 빈도를 분석 하여 적절한 DB Lock 동시성 제어 방식을 선택해야할 것 같습니다.

### 4. 분산 락을 구현하고 동시성을 제어 (feat. Redis)

분산 락(Distributed Lock)은 여러 클라이언트가 분산된 환경에서 공유 자원에 대한 동시 접근을 제어하기 위해 사용하는 락입니다.

- 분산 시스템에서는 여러 노드나 서버가 동시에 동일한 자원에 접근하려 할 수 있기 때문에, 이러한 경쟁 상황에서 데이터 일관성을 유지하기 위해 락이 필요합니다.

#### 분산 락 구현 방식

- Redis 서버를 구축하고 Redisson 라이브러리를 사용하여 락을 생성하고, 락을 획득하고 해제하는 Simple Lock 방식으로 구현하였습니다.
- 어노테이션 기반으로 AOP를 이용해 분산락 컴포넌트를 만들게 되었습니다.

#### 코드 설명

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DistributedLock {
    String key();
    TimeUnit timeUnit() default TimeUnit.SECONDS;
    long waitTime() default 5L;
    long leaseTime() default 3L;
}
```

```java
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class DistributedLockAop {
    private static final String REDISSON_LOCK_PREFIX = "LOCK:";

    private final RedissonClient redissonClient;

    @Around("@annotation(hhplus.concert.support.annotation.DistributedLock)")
    public Object DistributedLock(final ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        DistributedLock distributedLock = method.getAnnotation(DistributedLock.class);

        // 1)
        String key = REDISSON_LOCK_PREFIX + CustomSpringELParser.getDynamicValue(signature.getParameterNames(), joinPoint.getArgs(), distributedLock.key());
        RLock rLock = redissonClient.getLock(key);
        try {
            boolean available = rLock.tryLock(distributedLock.waitTime(), distributedLock.leaseTime(), distributedLock.timeUnit());  // 2)

            if (!available) {
                log.info("Lock 획득 실패={}", rLock);
                return false;
            }
            // 3)
            return joinPoint.proceed();
        } catch (InterruptedException e) {
            throw new InterruptedException();
        } finally {
            try {
                // 4)
                rLock.unlock();
            } catch (IllegalMonitorStateException e) {
                log.info("Redisson Lock Already UnLock - serviceName: {}, lockKey: {}", method.getName(), key);
            }
        }
    }
}
```

1. `@DistributedSimpleLock` 커스텀 어노테이션 정의
   - 어노테이션을 만들어 분산 락을 적용할 메서드를 지정합니다.
   - 분산 락의 키(필수!), 대기 시간, 임대 시간 등을 커스텀하게 설정할 수 있도록 하였습니다.
   - 이를 통해 분산락 처리 로직을 비즈니스에 영향을 미치지 않고 분리해서 사용할 수 있게 됩니다.
2. `DistributedLockAop` AOP를 이용한 락 적용

   - `@DistributedSimpleLock` 어노테이션이 적용된 메서드 실행시 락을 획득하고 종료시 해제하도록 구현하였습니다.
   - 이를 통해 이후 분산락에 대한 추가 요구사항에 대해서 공통으로 관리할수 있게 되었습니다.

   `DistributedLockAop` 순서는 다음과 같습니다

   1. 락의 이름으로 RLock 인스턴스를 가져온다. Lock 의 고유 키 값은 '메서드 이름{공유 자원 키값}' 형식으로 지정.
   2. 정의된 waitTime까지 Lock 획득을 시도한다, 정의된 leaseTime이 지나면 잠금을 해제하도록 한다.
   3. DistributedLock 어노테이션이 선언된 메서드를 실행한다.
   4. 종료 시 무조건 락을 해제한다.

#### 분산락과 트랜잭션 순서를 보장하도록 기존 로직 변경

락과 트랜잭션은 데이터의 무결성을 보장하기 위해 아래 순서에 맞게 수행되도록 작성이 필요합니다.

```
'락 획득 → 트랜잭션 시작 → 비즈니스 로직 → 트랜잭션 종료(커밋 or 롤백) → 락 반납'
```

- 하지만 이전 처럼 Facade레벨에 `@Transactional` 어노테이션을 사용하게 되는 경우에 확실하게 순서를 보장한다고 할 수 없습니다.
- 따라서 이를 해결하기 위해 Facade-> Service 레벨로 트랜잭션 범위를 수정하여 락 획득 이후에 트랜잭션을 시작, 종료할 수 있도록 변경하였습니다.
- 이로 인해 추가적으로 다음과 같은 성능 개선 효과또한 기대할 수 있게 되었습니다.
  - 변경 이전: Facade로 여러 작업들이 하나의 트랜잭션으로 관리되어지고 있기 때문에,
    대기 시간이 증가하여 동시 처리 성능이 떨어질 수 있습니다.
  - 변경 이후: Service 레벨에서 관리하게 되어 더 작은 트랜잭션 범위를 제어함으로서 트랜잭션 유지 시간을 줄이고, 리소스 점유를 최소화할 수 있습니다.

#### 분산 락을 실제 로직에 적용

1. 좌석 예약(`ReservationFacade`)

```java
@DistributedLock(key = "#reservationRequest.seatId")
public ReservationServiceDto.Result createReservation(ReservationServiceDto.Request reservationRequest, String token) {

    validateQueueStatus(token);

    Reservation reservation = reservationService.createReservation(
            reservationRequest.userId(),
            reservationRequest.concertId(),
            reservationRequest.concertScheduleId(),
            reservationRequest.seatId()
    );

    return new ReservationServiceDto.Result(
            reservation.getId(),
            reservation.getConcert().getId(),
            reservation.getConcert().getTitle(),
            reservation.getSeat().getConcertSchedule().getConcertAt(),
            reservation.getSeat().getSeatNumber(),
            reservation.getSeat().getSeatPrice(),
            reservation.getStatus()
    );
}
```

- Facade 레벨의 `@Transactional` 을 제거하고, `ReservationService` 실행 시 트랜잭션이 동작하도록 추가해주었습니다.
- `@DistributedLock` 어노테이션을 읽어 들여 락을 획득하고, 이후 해당 메서드 `ReservationFacade.createReservation`의 비즈니스 로직을 실행합니다.
- `ReservationService` 에서 트랜잭션이 동작되고 비즈니스 로직을 실행, 트랜잭션을 종료합니다.
- `ReservationFacade.createReservation` 가 종료되면 락을 반환하게됩니다. 이후 대기중인 락이 실행됩니다.
- 이전에 진행한 동시성 테스트 코드를 정상적으로 통과합니다.

2. 잔액 충전(`BalanceFacade`)

```java
@DistributedLock(key = "#userId")
public BalanceServiceDto.Result chargeBalance(Long userId, int amount) {
    // 충전 금액이 0원 이하이면 에러
    if (amount <= 0){
        throw new BusinessException(ErrorCode.BALANCE_INVALID_CHARGE_AMOUNT);
    }
    Balance balanceResult = balanceService.charge(userId, amount);

    return new BalanceServiceDto.Result(
            balanceResult.getUser().getId(),
            balanceResult.getAmount()
    );
}
```

- Facade 레벨의 `@Transactional` 을 제거하고, `BalanceService` 실행 시 트랜잭션이 동작하도록 추가해주었습니다.
- `@DistributedLock` 어노테이션을 읽어 들여 락을 획득하고, 이후 해당 메서드 `BalanceFacade.chargeBalance`의 비즈니스 로직을 실행합니다.
- `BalanceService` 에서 트랜잭션이 동작되고 비즈니스 로직을 실행, 트랜잭션을 종료합니다.
- `BalanceFacade.chargeBalance` 가 종료되면 락을 반환하게됩니다. 이후 대기중인 락이 실행됩니다.
- 이전에 진행한 동시성 테스트 코드를 정상적으로 통과합니다.

#### 분산락 기술에 대한 고찰

1. 확장성
   - Redis와 같은 분산 시스템에서 작동하기때문에, 여러 인스턴스에서 쉽게 확장할 수 있습니다.
     이를 통해 수평 확장이 가능하고 성능을 향상시킬 수 있습니다.
   - 따라서, Redis를 이용한 분산 락은 여러 서버에서 동작하는 애플리케이션의 동시성 문제를 효과적으로 해결할 수 있습니다.
2. 독립성과 안정성
   - Redis와 같은 별도의 시스템에서 락을 관리하므로, 데이터베이스와 분리되어 있어 시스템 간의 독립성이 강화됩니다.
   - 데이터베이스로의 부하를 줄일 수 있어 안정성을 확보할 수 있습니다.
3. 비즈니스 로직과 분리
   - AOP와 커스텀 어노테이션을 통해 비즈니스 로직과 동시성 제어 로직을 분리할 수 있었습니다.
4. 동시성 제어 확인
   - 테스트 코드를 통해 분산 데이터의 정합성을 보장하는 것을 확인할 수 있었습니다.
5. 추가 고려 사항
   - Redis 서버의 장애 상황에 대한 대비책을 고려해야 합니다
   - Redis 에서는 이를 위해 여러 Redis 서버 인스턴스에서 락을 안전하게 관리할 수 있도록 RedLock 분산 락 매커니즘을 제공합니다.
   - 서버가 다운되는 경우, 비즈니스 요구 사항에 따라서 어떻게 선택할 수 있도록 할지 설계할 수 있도록 합니다.

### 5. 콘서트 예약 서비스에서 채택한 동시성 처리 방법

#### 1) 좌석 예약

Redis를 이용한 **분산 락(DistributedLock)** 처리를 통해 동시성 문제를 제어하도록 하였습니다. 채택하게 된 이유는 다음과 같습니다.

1. 확장성을 고려

   - 콘서트와 같은 이벤트, 특히 좌석 같은 한정 된 공유 자원에 대해서 대량의 동시 사용자 트래픽을 처리해야 할 수 있어야합니다.
   - 분산락을 사용하게 되면, (현재는 단일 서버에서 진행하였지만) 여러 서버에서 동시에 처리할 수 있으며, 시스템의 확장성을 높일 수 있습니다.

2. 비관적 락의 대안

   - 동시에 많은 요청이 들어와도 데이터 정합성을 지킬 수 있는 비관적 락과 분산 락 사이에서 고민하였습니다.
   - 하지만, 비관적 락은 리소스가 잠긴 동안 다른 요청은 기다려야 하며, 락을 해제하는 시점까지 대기하게 됩니다. 이는 사용자가 락을 오랜 시간 동안 유지하면 대기 시간이 길어질 수 있습니다.(데드락 문제를 일으킬 수도 있습니다.)
   - 하지만 분산락은 락을 얻지 못한 요청이 대기할 순 있지만, 재시도나 대기 타임아웃을 설정하여 대기 시간을 조절할 수가 있습니다. 따라서 효율적인 대기 처리로 대기 시간을 줄일 수 있습니다.

3. 장애 처리가 유연하고 데이터베이스의 안정성을 가져갈 수 있습니다.

   - 여러 Redis 인스턴스를 활용하는 RedLock과 같은 분산 락을 사용하면, 단일 서버의 장애로 인한 문제가 최소화됩니다. 하나의 Redis 서버가 다운되더라도 다른 서버에서 락을 관리할 수 있어, 시스템의 가용성을 높일 수 있습니다.
   - 락에 대한 처리를 데이터베이스가 아닌 외부 서버에서 처리하므로 안정성을 보장할 수 있습니다.

4. 비즈니스 로직이 오염되지 않게 분리해서 사용이 가능.

   - AOP와 커스텀 어노테이션을 통해 비즈니스 로직을 전혀건드리지 않고도 동시성 제어 로직을 적용시킬 수 있습니다.
   - 유지보수성과 가독성 향상에 많은 도움이 될 것 같습니다.

#### 2) 잔액 충전

**낙관적 락** 처리를 통해 동시성 문제를 제어하도록 하였습니다. 채택하게 된 이유는 다음과 같습니다.

1. 비즈니스 요구 사항을 고려

   - 일반적인 충전 프로세스에서 같은 사용자가 동시에 여러 번 충전 요청을 하는 경우는 주로 네트워크 지연이나 클라이언트 측의 실수로 발생할 수 있습니다. 이러한 상황에서는 요청이 중복되지 않도록 처리하는 것이 더 적합하다고 판단하여 낙관적 락을 선택했습니다.

2. 동시 요청 빈도수가 적음

   - 잔액 자원은 독립적입니다.
   - 각 사용자가 자신의 잔액만 수정하기 때문에, 다른 사용자의 잔액과는 독립적으로 작업할 수 있습니다. 이 경우 동시성 충돌이 발생할 가능성이 적어 낙관적 락을 사용하는 것이 유리하다고 생각합니다.
   - 적은 동시 요청에는 테스트를 통해 동시성 제어를 보장하는 것을 확인하였습니다.

3. 성능 최적화

   - 낙관적 락은 락을 획득하기 위해 대기할 필요가 없으며, 데이터 수정 시점에만 충돌을 검사하므로 일반적으로 더 빠르게 처리됩니다.

4. 유연한 처리가 가능

   - 사용자가 잔액 충전 요청을 하고 충돌이 발생했을 때 예외를 반환하고, 재시도하는 방식으로 유연한 처리가 가능합니다.
