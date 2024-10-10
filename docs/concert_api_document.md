# 1. 대기열 토큰 발급

대기열에 사용자를 추가하고 대기열 토큰을 반환하도록 합니다.

### Endpoint

- **URL**: `/queue/token/users/{userId}`
- **Method**: POST

### Request

- **URL Params**:
    - `userId`: Long (유저 ID)

### Response

```json
{
  "tokenId": 1,
  "createdAt": "2024-10-10T10:00:00",
  "expiredAt": "2024-10-10T10:05:00"
}
```

### Error

```json
{
  "code": 404,
  "message": "user not found"
}
```
<br>

# 2. 대기열 토큰 조회

사용자에게 발급된 토큰 정보를 조회합니다.

### Endpoint

- **URL**: `/queue/token/users/{userId}`
- **Method**: GET

### Request

- **URL Params**:
    - `userId`: Long (유저 ID)

### Response

```json
{
  "tokenId": 1,
  "createdAt": "2024-10-10T10:00:00",
  "expiredAt": "2024-10-10T10:05:00"
}
```

### Error

```json
{
  "code": 404,
  "message": "user not found"
}
```
<br>

# 3. 대기열 상태 조회

유저 토큰을 통해 사용자의 대기열 상태를 조회합니다.

### Endpoint

- **URL**: `/queue/status`
- **Method**: GET

### Request

- **Headers**:
    - **Authorization**: `Token` (대기열 토큰 정보)

### Response

```json
{
  "queueId": 1,
  "createdAt": "2024-10-10T10:00:00",
  "status": "WAIT",
}
```

### Error

```json
{
  "code": 401,
  "message": "invalid token"
}
```

```json
{
  "code": 404,
  "message": "token not found"
}
```
<br>

# 4. 콘서트 일정 조회

특정 콘서트의 예약 가능한 일정 목록을 조회합니다.

### Endpoint

- **URL**: `/concerts/{concertId}/schedules`
- **Method**: GET

### Request

- **URL Params**:
    - `concertId`: Long (콘서트 ID)
- **Headers**:
    - **Authorization**: `Token` (대기열 토큰 정보)

### Response

```json
{
  "concertId": 1,
  "schedules": [
    {
      "scheduleId": 1,
      "concertAt": "2024-10-10T10:00:00",
      "reservationAt": "2024-10-01T10:00:00"
    },
    {
      "scheduleId": 2,
      "concertAt": "2024-10-17T10:00:00",
      "reservationAt": "2024-10-10T10:00:00"
    }
  ]
}
```

### Error

```json
{
  "code": 401,
  "message": "invalid token"
}
```

```json
{
  "code": 404,
  "message": "user not token"
}
```

```json
{
  "code": 404,
  "message": "concert not found"
}
```
<br>

# 5. 콘서트 좌석 조회

특정 콘서트의 좌석 정보를 조회합니다

### Endpoint

- **URL**: `/concerts/{concertId}/schedules/{scheduleId}/seats`
- **Method**: GET

### Request

- **URL Params**:
    - `concertId`: Long (콘서트 ID)
    - `scheduleId`: Long (일정 ID)
- **Headers**:
    - **Authorization**: `Token` (대기열 토큰 정보)

### Response

```json
{
  "concertId": 1,
  "concertAt": "2024-10-10T10:00:00",
  "seats": [
    {
      "seatId": 1,
      "seatNumber": 1,
      "seatStatus": "AVAILABLE",
      "seatPrice": 10000
    },
    {
      "seatId": 2,
      "seatNumber": 2,
      "seatStatus": "UNAVAILABLE",
      "seatPrice": 10000
    }
    ...
     {
      "seatId": 50,
      "seatNumber": 50,
      "seatStatus": "UNAVAILABLE",
      "seatPrice": 10000
    }
  ]
}

```

### Error

```json
{
  "code": 401,
  "message": "invalid token"
}

```

```json
{
  "code": 404,
  "message": "token not found"
}

```

```json
{
  "code": 404,
  "message": "concert not found"
}

```

```json
{
  "code": 404,
  "message": "concert schedule not found"
}

```
<br>

# 6. 좌석 예약

콘서트 좌석을 예약합니다.

### Endpoint

- **URL**: `/concerts/reservations`
- **Method**: POST

### Request

- **Headers**:
    - `Content-Type`: application/json
    - **Authorization**: `Token` (대기열 토큰 정보)
- **Body**:

```json
{
  "userId": 1,
  "concertId": 1,
  "scheduleId": 1,
  "seatId": 20
}
```

### Response

```json
{
  "reservationId": 1,
  "concertId": 1,
  "concertName": "아이유 콘서트",
  "concertAt": "2024-10-10T10:00:00",
  "seatNumber": 10,
  "seatprice": 10000
  "reservationStatus": "PAYMENT_PENDING"
}
```

### Error

```json
{
  "code": 401,
  "message": "invalid token"
}

```

```json
{
  "code": 404,
  "message": "token not found"
}

```

```json
{
  "code": 404,
  "message": "concert not found"
}

```

```json
{
  "code": 404,
  "message": "concert schedule not found"
}

```

```json
{
  "code": 404,
  "message": "seat not found"
}

```

```json
{
  "code": 400,
  "message": "reservation failed"
}

```
<br>

# 7. 결제 실행

콘서트 좌석 예약에 대한 결제를 진행합니다.

- 예약을 5분내에 결제하지 않으면 결제할 수 없습니다.

### Endpoint

- **URL**: `/payment/concerts/users/{userId}`
- **Method**: POST

### Request

- **URL Params**:
    - `userId`: Long (사용자 ID)
- **Headers**:
    - `Content-Type`: application/json
    - **Authorization**: `Token` (대기열 토큰 정보)
- **Body**:

```json
{
  "reservationId": 1
}
```

### Response

```json
{
  "paymentId": 1,
  "amount": 10000,
  "paymentStatus": "COMPLETED"
}
```

### Error

```json
{
  "code": 401,
  "message": "invalid token"
}
```

```json
{
  "code": 404,
  "message": "token not found"
}
```

```json
{
  "code": 404,
  "message": "user not found"
}
```

```json
{
  "code": 404,
  "message": "reservation not found"
}
```

```json
{
  "code": 400,
  "message": "Insufficient balance"
}

```

```json
{
  "code": 500,
  "message": "payment failed"
}
```
<br>

# 8. 잔액 충전

사용자의 잔액을 충전합니다.

### Endpoint

- **URL**: `/balance/users/{userId}/charge`
- **Method**: POST

### Request

- **URL Params**:
    - `userId`: Long (사용자 ID)
- **Headers**:
    - `Content-Type`: application/json
- **Body**:

```json
{
  "amount": 10000
}
```

### Response

```json
{
  "userId": 1,
  "currentAmount": 20000
}
```

### Error

```json
{
  "code": 404,
  "message": "user not found"
}

```

```json
{
  "code": 400,
  "message": "invalid charge amount"
}

```

```json
{
  "code": 500,
  "message": "charge failed"
}

```
<br>

# 9. 잔액 조회

사용자의 현재 잔액을 조회합니다.

### Endpoint

- **URL**: `/balance/users/{userId}`
- **Method**: GET

### Request

- **URL Params**:
    - `userId`: Long (사용자 ID)

### Response

```json
{
  "userId": 1,
  "currentAmount": 20000
}

```

### Error

```json
{
  "code": 404,
  "message": "user not found"
}
```
