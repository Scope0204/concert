# 콘서트 예약 서비스 인덱스 도입

### 1. 인덱스가 필요한 쿼리 분석 및 도입

콘서트 예약 시스템에서 사용 중인 주요 쿼리들을 분석하고, 인덱스를 도입하기에 적합한 쿼리에 적용 및 **예상 계획**과 **실행 시간**을 확인합니다.

- `EXPLAIN`: 쿼리를 실행하지 않고 예상 계획만 확인.
- `EXPLAIN ANALYZE`: 쿼리를 실제로 실행하고 실제 실행 통계까지 확인.
- 본 프로젝트는 `JPA`를 사용하므로, JPA 의 DDL(Data Definition Language) 생성 기능을 활용하여 인덱스를 선언적으로 추가하도록 하였습니다.

#### 1.1. 사용자 잔액 조회 (Balance)

```java
public Balance findByUserId(Long userId) {
        return jpaBalanceRepository.findByUserId(userId);
}
```

- 기능 : 유저 ID 로 잔액 정보를 조회
- 실제 SQL

  ```sql
  SELECT b.* FROM Balance b WHERE b.user.id = :userId
  ```

- 인덱스 적용 이유
  - 예약, 결제, 잔액 등 다양한 도메인의 비즈니스 로직에서 "사용자 별 잔액 조회" 가 빈번하게 일어날 것으로 예상
  - 사용자가 많아 질수록, 그에 따라 테이블 크기가 커지므로 인덱스 없이는 성능 저하가 예상
- 인덱스 적용

  ```java
  @Entity
  @Table(name = "balance", indexes = @Index(name = "idx_user_id", columnList = "user_id"))
  public class Balance {
  }
  ```

  - 높은 Cardinality 가 예상되는 `user_id` 컬럼에 인덱스를 적용

- 인덱스 적용 전

  **1. `EXPLAIN` 결과 확인**

  ```sql
  EXPLAIN SELECT * FROM balance WHERE user_id = 4;
  ```

  | id  | select_type | table   | partitions | type | possible_keys | key  | key_len | ref  | rows | filtered | Extra       |
  | :-- | :---------- | :------ | :--------- | :--- | :------------ | :--- | :------ | :--- | :--- | :------- | :---------- |
  | 1   | SIMPLE      | balance | null       | ALL  | null          | null | null    | null | 3000 | 10       | Using where |

  - 전체 테이블 스캔을 수행하고 있습니다.
  - 조회 `rows` 가 3000개 임을 확인할 수 있습니다.

  **2. `EXPLAIN ANALYZE` 결과 확인**

  ```sql
  EXPLAIN ANALYZE SELECT * FROM balance WHERE user_id = 4;
  ```

  ```
  -> Filter: (balance.user_id = 4)  (cost=303 rows=300) (actual time=1.69..1.69 rows=2 loops=1)
    -> Table scan on balance  (cost=303 rows=3000) (actual time=0.138..1.5 rows=3002 loops=1)
  ```

  - `balance` 테이블을 전체 스캔합니다(3002개 행). 테이블 전체를 확인하여 `balance.user_id = 4` 조건에 맞는 행을 필터링하여 데이터를 찾습니다.
  - 비용(cost): 쿼리 플래너가 예측한 비용이 303이며, 약 3000개의 행을 확인할 것으로 예상합니다.
  - 실제 수행시간: 실제로는 약 1.5ms가 소요되었습니다. 첫 행을 가져오는 데 0.138ms가 걸렸고, 마지막 행까지 확인하는 데 총 1.5ms가 걸렸습니다.

- 인덱스 적용 후

  **1. `EXPLAIN` 결과 확인**
  | id | select_type | table | partitions | type | possible_keys | key | key_len | ref | rows | filtered | Extra |
  | :-- | :---------- | :------ | :--------- | :--- | :------------ | :---------- | :------ | :---- | :--- | :------- | :---- |
  | 1 | SIMPLE | balance | null | ref | idx_user_id | idx_user_id | 9 | const | 2 | 100 | Using index condition |

  - 인덱스를 사용 하여(`type: ref`) 조회 하는 것을 확인할 수 있습니다.
  - 조회해야 할 `rows` 가 2개로 줄었습니다.

  **2. `EXPLAIN ANALYZE` 결과 확인**

  ```
  -> Index lookup on balance using idx_user_id (user_id=4)  (cost=0.7 rows=2) (actual time=0.03..0.0319 rows=2 loops=1)
  ```

  - 인덱스(`idx_user_id`)를 통해 `user_id = 4` 조건에 맞는 데이터를 바로 찾아갑니다. 이는 테이블 스캔 없이 인덱스를 조회하여 데이터를 가져오기 때문에 훨씬 효율적입니다.
  - 비용(cost): 인덱스를 사용해 데이터 접근 비용이 0.7로 크게 줄었으며, 예상 조회 행 수는 2입니다.
  - 실제 수행시간: 실제 실행 시간은 약 0.03ms로, 첫 번째 행을 가져오는 데 0.03ms가 걸렸으며 전체 조회가 0.0319ms 에 완료되었습니다.

#### 1.2. 콘서트 스케줄 조회 (Concert)

```java
public List<ConcertSchedule> findAllByConcertId(Long concertId) {
        return jpaConcertScheduleRepository.findAllByConcertId(concertId);
}
```

- 기능 : 콘서트 ID 로 콘서트 스케쥴 정보를 조회
- 실제 SQL

  ```sql
    SELECT cs.* FROM concert_schedule cs WHERE cs.concert_id = :concert_id
  ```

- 인덱스 적용 이유

  - 콘서트 예약 이전에, 콘서트 일정을 조회하는 경우가 빈번할 것으로 예상
  - 콘서트 수가 사용자가 많아 질수록, 그에 따라 테이블 크기가 커지므로 인덱스 없이는 성능 저하가 예상

- 인덱스 적용

  ```java
    @Entity
    @Table(name = "concert_schedule", indexes = @Index(name = "idx_concert_id", columnList = "concert_id"))
    public class ConcertSchedule {
    }
  ```

  - 높은 Cardinality 가 예상되는 `concert_id` 컬럼에 인덱스를 적용

- 인덱스 적용 전

  **1. `EXPLAIN` 결과 확인**

  ```sql
  EXPLAIN select * from concert_schedule where concert_id = 2;
  ```

  | id  | select_type | table            | partitions | type | possible_keys | key  | key_len | ref  | rows | filtered | Extra       |
  | :-- | :---------- | :--------------- | :--------- | :--- | :------------ | :--- | :------ | :--- | :--- | :------- | :---------- |
  | 1   | SIMPLE      | concert_schedule | null       | ALL  | null          | null | null    | null | 1006 | 10       | Using where |

  - 전체 테이블 스캔을 수행하고 있습니다.
  - 조회 `rows` 가 1006개 임을 확인할 수 있습니다.

  **2. `EXPLAIN ANALYZE` 결과 확인**

  ```sql
  EXPLAIN ANALYZE select * from concert_schedule where concert_id = 2;
  ```

  ```
  -> Filter: (concert_schedule.concert_id = 2)  (cost=102 rows=101) (actual time=0.764..0.766 rows=6 loops=1)
  -> Table scan on concert_schedule  (cost=102 rows=1006) (actual time=0.178..0.7 rows=1006 loops=1)
  ```

  - `concert_schedule` 테이블 전체를 스캔하고, `concert_schedule.concert_id = 2` 조건을 기준으로 필터 조건을 만족하는 레코드를 찾습니다.
  - 비용(cost): 약 102의 비용이 발생했으며, 예상 행 수는 1006개입니다.
  - 실제 수행 시간: 0.178초에서 시작해 0.7초까지 소요, 결과적으로 6개의 레코드를 찾았습니다.

- 인덱스 적용 후

  **1. `EXPLAIN` 결과 확인**

  | id  | select_type | table            | partitions | type | possible_keys  | key            | key_len | ref   | rows | filtered | Extra                 |
  | :-- | :---------- | :--------------- | :--------- | :--- | :------------- | :------------- | :------ | :---- | :--- | :------- | :-------------------- |
  | 1   | SIMPLE      | concert_schedule | null       | ref  | idx_concert_id | idx_concert_id | 9       | const | 6    | 100      | Using index condition |

  - 인덱스를 사용 하여(`type: ref`) 조회 하는 것을 확인할 수 있습니다.
  - 조회해야 할 `rows` 가 6개로 줄었습니다.

  **2. `EXPLAIN ANALYZE` 결과 확인**

  ```
  -> Index lookup on concert_schedule using idx_concert_id (concert_id=2)  (cost=2.1 rows=6) (actual time=0.144..0.146 rows=6 loops=1)
  ```

  - `idx_concert_id` 인덱스를 사용하여 `concert_id=2` 조건을 만족하는 레코드를 바로 찾습니다.
  - 비용(cost): 약 2.1로 크게 감소하였으며, 예상 행 수는 6개입니다.
  - 실제 수행 시간: 0.144초에서 시작해 0.146초까지 소요, 동일하게 6개의 레코드를 찾았습니다. 데이터 수가 적어 실제 수행시간에 차이가 미비함을 확인할 수 있습니다.

#### 1.3. 만료된 예약 정보 조회 (Reservation)

findExpiredReservations

```java
public List<Reservation> findExpiredReservations(ReservationStatus reservationStatus, LocalDateTime expirationTime) {
        return jpaReservationRepository.findExpiredReservations(reservationStatus, expirationTime);
}
```

- 기능 : 예약 상태가 만료 된 예약들을 조회(스케줄러에서 사용)
- 실제 SQL

  ```sql
    SELECT r FROM Reservation r WHERE r.status = :reservationStatus AND r.reservationAt < :expirationTime
  ```

- 인덱스 적용 이유
  - 만료된 예약을 정기적으로 조회하는 스케쥴러 작업에서 사용된다.
  - 예약 상태와 생성 시간을 함께 고려해야 하므로, 두가지를 조합한 복합 인덱스가 효과적일 것으로 판단했다.
  - 대량의 예약 데이터에서 조건에 맞는 레코드를 빠르게 찾아야 한다.
- 인덱스 적용

  ```java
    @Entity
    @Table(name = "reservation", indexes = {
            @Index(name = "idx_status_reservation_at", columnList = "status, reservation_at")
    })
    public class Reservation {
    }
  ```

  - 두 컬럼을 동시에 사용하는 복합 조건이 사용되므로 `status` 와 `reservation_at` 컬럼에 복합 인덱스 적용
  - `status` 컬럼은 낮은 Cardinality를 가질 것으로 예상 (예: '대기중', '완료', '취소', '실패' 등 몇 가지 상태만 존재). 따라서 해당 컬럼에 인덱스를 걸더라도 많은 레코드를 거쳐야 할 가능성이 커질 수 있음
  - 그러나 `reservation_at` 컬럼은 높은 Cardinality를 가질 것으로 예상 (각 예약마다 고유한 생성 시간을 가짐)
  - 복합 인덱스를 사용함으로써, 낮은 Cardinality의 `status` 로 1차 필터링을 하고, 높은 Cardinality의 `reservation_at` 으로 2차 필터링을 수행하여 효율적인 검색이 가능할 것으로 기대

- 인덱스 적용 전

  **1. `EXPLAIN` 결과 확인**

  ```sql
  EXPLAIN select * from reservation where status='PENDING' and reservation_at < '2024-03-03 10:34:49' ;
  ```

  | id  | select_type | table       | partitions | type | possible_keys | key  | key_len | ref  | rows | filtered | Extra       |
  | :-- | :---------- | :---------- | :--------- | :--- | :------------ | :--- | :------ | :--- | :--- | :------- | :---------- |
  | 1   | SIMPLE      | reservation | null       | ALL  | null          | null | null    | null | 3000 | 8.33     | Using where |

  - 전체 테이블 스캔을 수행하고 있습니다.
  - 조회 `rows` 가 3000 개 임을 확인할 수 있습니다.

  **2. `EXPLAIN ANALYZE` 결과 확인**

  ```sql
  EXPLAIN ANALYZE select  * from reservation where status='PENDING' and reservation_at < '2024-03-03 10:34:49' ;
  ```

  ```
    -> Filter: ((reservation.`status` = 'PENDING') and (reservation.reservation_at < TIMESTAMP'2024-03-03 10:34:49'))  (cost=304 rows=250) (actual time=1.42..5.33 rows=75 loops=1)
    -> Table scan on reservation  (cost=304 rows=3000) (actual time=0.128..2.28 rows=3000 loops=1)
  ```

  - `reservation` 테이블의 모든 행(3000개)을 순차적으로 읽어 필터링합니다. `status = 'PENDING' AND reservation_at < '2024-03-03 10:34:49'` 조건을 통해 `status`와 `reservation_at` 기준으로 데이터를 필터링합니다.
  - 비용(cost): 쿼리 플래너가 예상하는 비용은 304입니다.
  - 실제 실행 시간: 약 0.128ms ~ 2.28ms.

- 인덱스 적용 후

  **1. `EXPLAIN` 결과 확인**
  | id | select_type | table | partitions | type | possible_keys | key | key_len | ref | rows | filtered | Extra |
  | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
  | 1 | SIMPLE | reservation | null | range | idx_status_reservation_at | idx_status_reservation_at | 9 | null | 38 | 100 | Using index condition |

  - 인덱스를 사용 하여(`type: ref`) 조회 하는 것을 확인할 수 있습니다.
  - 조회해야 할 `rows` 가 38개로 줄었습니다.

  **2. `EXPLAIN ANALYZE` 결과 확인**

  ```
  -> Index range scan on reservation using idx_status_reservation_at over (status = 'PENDING' AND reservation_at < '2024-03-03 10:34:49.000000'), with index condition: ((reservation.`status` = 'PENDING') and (reservation.reservation_at < TIMESTAMP'2024-03-03 10:34:49'))  (cost=34 rows=75) (actual time=0.12..0.365 rows=75 loops=1)

  ```

  - `idx_status_reservation_at` 인덱스를 통해 `status와` `reservation_at` 조건에 맞는 행을 조회합니다.
  - `status = 'PENDING' AND reservation_at < '2024-03-03 10:34:49'` 조건을 통해 `status`와 `reservation_at` 기준으로 데이터를 필터링합니다.
  - 비용(cost): 쿼리 플래너가 예상하는 비용은 34로 크게 감소하였습니다.
  - 실제 실행 시간: 약 0.12ms ~ 0.365ms. 실행 시간이 조금 감소하였습니다.

### 2. 인덱스 도입 후 성능 향상 수치 확인

- 인덱스 적용 전: `O(n)` - 전체 테이블 스캔
- 인덱스 적용 후: `O(log n)` - `B+ Tree` 인덱스 검색(`Mysql` 기준)
- 실제 개선 사항 확인 결과

  - 인덱스 적용 전: 테이블의 모든 행을 확인하여 필터링하므로, 실행 비용이 크고 시간이 더 많이 소요됩니다.
  - 인덱스 적용 후: 인덱스를 통해 직접 접근하므로, 실행 시간이 크게 단축되었습니다.(90~95% 정도의 성능 향상 수치를 확인. **그러나 너무 적은 수의 경우 효과가 미비할 수 있음을 확인.**)

### 3. 결론

테스트를 통해 콘서트 예약 시스템의 주요 쿼리들에 대한 인덱스 적용 방안을 검토하였습니다. 결론은 다음과 같습니다.

- 인덱스를 적용함으로서 약간의 쓰기 성능이 저하될 수 있으나, 읽기 작업이 크게 단축될 수 있었습니다.

- 그러나 데이터가 너무 적은 경우에는 읽기 성능 개선이 미비하기때문에, 쓰기 성능을 저하시킬 수 있으므로 규모에 따라서 주의해서 사용할 필요성이 있습니다.

- 서비스에서는 읽기 작업이 대부분의 쿼리 호출의 비중을 차지하므로, 서비스 규모가 커질수록 사용자 경험은 크게 개선될 수 있을 것으로 예상됩니다.

- 읽기 작업에 대한 리스크가 줄어듬으로서, 콘서트 예약 시스템의 전반적인 성능과 확장성 또한 크게 개선될 것으로 예상됩니다.
