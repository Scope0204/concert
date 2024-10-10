## 1. 유저 토큰 발급

### Sequence Diagram

```mermaid
sequenceDiagram
    actor User
    participant Controller
    participant UserService
    participant TokenService
    participant QueueService
    participant Database

    User->>Controller: 토큰 발급 요청
    Controller->>UserService: 유저 인증 요청 (사용 가능한 유저인지)
    UserService->>Database: 유저 정보 조회
    Database-->>UserService: 유저 정보 반환
    alt 유저 정보가 존재 (사용 가능한 유저)
        UserService-->>Controller: 유저 인증 결과 반환
		Controller->>QueueService: (토큰 정보 추가를 위한) 대기열 상태 조회 요청
		QueueService->>Database: 현재 대기열 상태 조회
		Database-->>QueueService: 현재 대기열 상태 반환
		QueueService-->>Controller: 대기열 순번 결과 반환
        Controller->>TokenService: 토큰 생성 요청
        TokenService->>Database: 토큰 생성 후 정보 저장
        Database-->>TokenService: 저장 완료
        TokenService-->>Controller: 생성된 토큰 전달
        Controller-->>User: 토큰 정보 및 대기열 상태 정보 반환
    else 존재하지 않는 유저 (인증 실패)
        UserService-->>Controller: 에러 메시지 반환(NOT FOUND)
        Controller-->>User: 에러 코드, 메시지 반환
    end
```

### Description

유저가 콘서트 예약을 시도할 때, 토큰을 발급받습니다.

현재 대기열의 상태를 조회하여, 토큰 생성을 요청을 하여 DB에 저장하도록 합니다.

생성된 토큰와 대기열의 상태 정보를 반환합니다.

## 2. 유저 토큰을 통한 대기열 정보 조회

### Sequence Diagram

```mermaid
sequenceDiagram
    participant User
    participant Controller
    participant TokenService
    participant QueueService
    participant Database

	User->>Controller: 토큰을 통해 대기열 정보 조회 요청
    Controller->>TokenService: 토큰 유효성 검증
    TokenService->>Database: 토큰 정보 조회
    Database-->>TokenService: 토큰 정보 조회 결과 반환
    alt 유효한 토큰인 경우
        TokenService-->>Controller: 토큰 정보 조회 결과 반환
		Controller->>QueueService: 현재 사용자의 콘서트 대기열 상태 요청
		QueueService->>Database: 현재 콘서트 대기열 상태 조회
		Database-->>QueueService: 현재 콘서트 대기열 상태 반환
		QueueService-->>Controller: 사용자의 현재 대기열 상태 반환
		Controller-->>User: 사용자의 현재 대기열 상태 결과 반환
	else 유효하지 않거나 토큰이 없는 경우
		TokenService-->>Controller: 에러 메시지 반환
		Controller-->>User: 에러 코드, 메시지 반환
    end
```

### Description

토큰을 통해 대기열 정보를 조회 합니다.

기본적으로 “폴링” 으로 본인의 대기열을 확인한다고 가정합니다.

## 3. 예약 가능 날짜 목록 조회

### Sequence Diagram

```mermaid
sequenceDiagram
	actor User
	participant Controller
	participant TokenService
	participant ConcertService
	participant Database

	User->>Controller: 예약 가능한 콘서트 날짜 목록 조회 요청
	Controller->>TokenService: 토큰 유효성 검증
	TokenService->>Database: 토큰 정보 조회
	Database-->>TokenService: 토큰 정보 조회 결과 반환
	alt 유효한 토큰 정보인 경우
		TokenService-->>Controller: 토큰 정보 조회 결과 반환
		Controller->>ConcertService: 예약 가능한 콘서트 날짜 목록 조회
		ConcertService->>Database: 콘서트 날짜 목록 조회
		Database-->>ConcertService: 콘서트 날짜 목록 반환
		ConcertService-->>Controller: 예약 가능한 콘서트 날짜 목록 반환
		Controller-->>User: 예약 가능한 콘서트 날짜 목록 반환
	else 유효하지 않거나 토큰이 없는 경우
		TokenService-->>Controller: 에러 메시지 반환
		Controller-->>User: 에러 코드, 메시지 반환
	end

```

### Description

유저 토큰이 유효한지 검사합니다.

토큰이 유효하다면, 선택한 콘서트가 진행되는 날짜 목록을 조회 합니다.

그 중, 예약이 가능한 날짜 목록을 사용자에게 반환하도록 합니다.

그러나, 토큰이 유효하지 않거나 없는 경우에는 예외를 반환합니다.

## 4. 좌석 정보 조회

### Sequence Diagram

```mermaid
sequenceDiagram
	actor User
	participant Controller
	participant TokenService
	participant SeatService
	participant Database

	User->>Controller: 좌석 목록 조회 요청
	Controller->>TokenService: 토큰 유효성 검증
	TokenService->>Database: 토큰 정보 조회
	Database-->>TokenService: 토큰 정보 조회 결과 반환
	alt 유효한 토큰 정보인 경우
		TokenService-->>Controller: 토큰 정보 조회 결과 반환
		Controller->>SeatService: 예약 가능한 좌석 목록 조회
		SeatService->>Database: 좌석 목록 조회
		Database-->>SeatService: 좌석 목록 반환(1~50)
		SeatService-->>Controller: 예약 가능한 좌석 목록 반환
	else 유효하지 않은 토큰 혹은 없는 경우
		TokenService-->>Controller: 에러 메시지 반환
		Controller-->>User: 에러 코드, 메시지 반환
	end
```

### Description

유저 토큰이 유효한지 검사합니다.

토큰이 유효하다면, 해당 콘서트가 열리는 날짜의 예약 가능한 좌석 목록을 조회합니다.(좌석 정보는 1~50 까지 좌석 번호로 관리 됩니다)

이후 사용자에게 예약 가능한 좌석 정보를 반환합니다.

## 5. 좌석 예약

### Sequence Diagram

```mermaid
sequenceDiagram
	actor User
	participant Controller
	participant TokenService
	participant ReservationService
	participant Database

	User->>Controller: 좌석 예약 요청
	Controller->>TokenService: 토큰 유효성 검증
	TokenService->>Database: 토큰 정보 조회
	Database-->>TokenService: 토큰 정보 조회 결과 반환
	alt 유효한 토큰 정보인 경우
		TokenService-->>Controller: 토큰 정보 조회 결과 반환
		Controller->>ReservationService: 콘서트 좌석 예약 요청
		ReservationService->>Database: 콘서트 좌석 상태 확인
		Database-->>ReservationService: 콘서트 좌석 상태 반환
		alt 콘서트 좌석에 여유 자리가 있는 경우
			ReservationService->>Database: 지정된 좌석 상태를 변경
			Database-->>ReservationService: 지정된 좌석 상태 변경 사항 반환
			ReservationService-->>Controller: 좌석 예약 성공 상태 반환
			note right of User: 예약 성공 후 결제 진행 (별도 프로세스)
			Note over ReservationService, Database: 5분 후 결제 미처리 시 좌석 예약 취소
			alt 좌석 예약 후 5분간 결제 미처리 시
				ReservationService->>Database: 좌석 상태를 다시 예약 가능으로 변경
				Database-->>ReservationService: 좌석 상태 변경 완료
			end
		else 콘서트 좌석에 여유 자리가 없는 경우
			ReservationService-->>Controller: 좌석 예약 실패 메시지 반환
			Controller-->>User: 에러 코드 반환
		end
	else 유효하지 않은 토큰 혹은 없는 경우
			TokenService-->>Controller: 에러 메시지 반환
			Controller-->>User: 에러 코드, 메시지 반환
	end
```

### Description

유저 토큰이 유효한지 검사합니다.

토큰이 유효하다면, 예약 가능한 날짜의 콘서트 좌석 상태를 확인합니다.

해당 좌석이 예약이 가능하다면 예약 정보를 저장하고, 좌석 상태를 임시 예약 상태로 변경합니다.

임시 예약 상태로 변경 후 결제 프로세스를 진행합니다. (이는 별도로 진행합니다.)

임시 예약 상태 이후 5분이 지났음에도 결제가 이루어지지 않은경우, 스케줄러를 통해 좌석 상태를 다시 변경해두도록합니다.(임시 예약 -> 예약 가능)

좌석이 예약 불가능한경우 예약 실패 메세지를 유저에게 반환합니다.

## 6. 결제

### Sequence Diagram

```mermaid
sequenceDiagram
	actor User
	participant Controller
	participant TokenService
	participant PaymentService
	participant ReservationService
	participant Database

	User->>Controller: 결제 요청
	Controller->>TokenService: 토큰 유효성 검증
	TokenService->>Database: 토큰 정보 조회
	Database-->>TokenService: 토큰 정보 조회 결과 반환
	alt 유효한 토큰 정보인 경우
		# 결제 요청. 결제 내역 반환
		TokenService-->>Controller: 토큰 정보 조회 결과 반환
		Controller->>PaymentService: 결제 처리 요청
		PaymentService->>Database: 유저 잔액 확인
		Database-->>PaymentService: 유저 잔액 반환
		alt 유저 잔액이 충분한 경우
			PaymentService->>Database: 결제 처리
			Database-->>PaymentService: 결제 처리 후 결과 반환
			PaymentService-->>Controller: 결제 처리 및 결제 내역 반환
			# 좌석 소유권을 유저에게 배정
			Controller->>ReservationService: 좌석 소유권 상태 변경 요청
			ReservationService->>Database: 좌석 상태 변경 요청
			Database-->>ReservationService: 좌석 상태 변경 요청 사항 반환
			ReservationService-->>Controller: 좌석 소유권 성태 변경 결과 전달
			# 대기열 토큰 만료 요청
			Controller->>TokenService: 대기열 토큰 만료 요청
			TokenService->>Database: 대기열 토큰 만료 처리
			Database-->>TokenService: 대기열 토큰 만료 처리 결과 반환
			TokenService-->>Controller: 대기열 토큰 만료 처리 결과 전달
			# 최종 결과 반환
			Controller-->>User: 성공 결과 및 결제 내역 전달
		else 유저 잔액이 부족한 경우
			PaymentService-->>Controller: 결제 실패 메시지 반환
			Controller-->>User: 에러 코드 반환
		end
	else 유효하지 않은 토큰 혹은 없는 경우
			TokenService-->>Controller: 에러 메시지 반환
			Controller-->>User: 에러 코드, 메시지 반환
	end
```

### Description

유저 토큰이 유효한지 검사합니다.

토큰이 유효하다면, 유저 잔액 정보를 먼저 확인합니다.

유저 잔액이 충분한 경우 결제 처리를 진행합니다.

결제에 성공하면 좌석의 상태를 결제 완료로 변경합니다.

또한, 대기열 토큰을 만료 시킵니다.

사용자에게는 성공 결과 및 결재 내역을 같이 전달합니다.

## 7. 잔액 충전

### Sequence Diagram

```mermaid
sequenceDiagram
    actor User
    participant Controller
    participant BalanceService
    participant Database

	User->>Controller: 잔액 충전 요청
    alt 충전 금액이 0원 이하인 경우
        Controller-->>User: 충전 금액 오류 메시지 반환
    else 충전 금액이 0원 이상인 경우
        Controller->>BalanceService: 잔액 충전 요청
        BalanceService->>Database: 잔액 충전 요청
        Database-->>BalanceService: 잔액 충전 후 결과 반환
        BalanceService-->>Controller: 충전 성공 메시지 및 충전 결과 반환
        Controller-->>User: 충전 성공 메시지 및 충전 결과 전달
    end
```

### Description

잔액 충전과 조회는 유저 토큰 검증이 불필요합니다.

충전 금액을 확인하여 0원 이상인 경우에만 잔액 충전을 진행하도록 합니다.

잔액 충전이 성공하면 총 잔액 결과를 반환합니다.

## 8. 잔액 조회

### Sequence Diagram

```mermaid
sequenceDiagram
    actor User
    participant Controller
    participant BalanceService
    participant Database

    User->>Controller: 잔액 조회 요청
    Controller->>BalanceService: 잔액 조회 요청
    BalanceService->>Database: 잔액 조회
    Database-->>BalanceService: 잔액 정보 반환
    BalanceService-->>Controller: 잔액 조회 결과 반환
    Controller-->>User: 잔액 조회 결과 전달
```

### Description

잔액 충전과 조회는 유저 토큰 검증이 불필요합니다.

유저의 현재 잔액을 조회합니다.
