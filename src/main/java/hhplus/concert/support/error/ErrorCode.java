package hhplus.concert.support.error;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON-01","서버에 문제가 발생하였습니다."),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND,"COMMON-02","찾을 수 없습니다."),
    CLIENT_ERROR(HttpStatus.BAD_REQUEST,"COMMON-03","잘못된 요청입니다."),

    USER_NOT_FOUND(HttpStatus.NOT_FOUND,"USER-01","사용자를 찾을 수 없습니다."),

    QUEUE_NOT_FOUND(HttpStatus.NOT_FOUND,"QUEUE-01","해당 대기열을 찾을 수 없습니다."),
    QUEUE_NOT_ALLOWED(HttpStatus.BAD_REQUEST,"QUEUE-02","해당 대기열은 허용되지 않습니다."),

    CONCERT_NOT_FOUND(HttpStatus.NOT_FOUND,"CONCERT-01","콘서트 정보를 찾을 수 없습니다."),
    CONCERT_UNAVAILABLE(HttpStatus.BAD_REQUEST,"CONCERT-02","해당 콘서트는 이용할 수 없습니다."),
    CONCERT_SCHEDULE_NOT_FOUND(HttpStatus.NOT_FOUND,"CONCERT-03","해당 콘서트 일정을 찾을 수 없습니다."),
    CONCERT_SEAT_NOT_FOUND(HttpStatus.NOT_FOUND,"CONCERT-04","해당 좌석 정보를 찾을 수 없습니다."),
    CONCERT_SCHEDULE_NOT_AVAILABLE(HttpStatus.OK,"CONCERT-05","콘서트 신청기간이 지났습니다."),
    CONCERT_SEAT_NOT_AVAILABLE(HttpStatus.OK,"CONCERT-06","해당 콘서트 좌석은 이미 예약 된 상태입니다."),

    RESERVATION_NOT_FOUND(HttpStatus.NOT_FOUND,"RESERVATION-01","예약 정보를 찾을 수 없습니다."),

    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND,"PAYMENT-01","결제 정보를 찾을 수 없습니다."),
    PAYMENT_INSUFFICIENT_BALANCE(HttpStatus.OK,"PAYMENT-02","결제 잔액이 부족합니다."),

    BALANCE_NOT_FOUND(HttpStatus.NOT_FOUND,"BALANCE-01","잔액을 찾을 수 없습니다."),
    BALANCE_INVALID_CHARGE_AMOUNT(HttpStatus.BAD_REQUEST,"BALANCE-02","유효하지 않은 충전 금액입니다.");

    private final HttpStatus status;
    private final String code;
    private final String description;
}


