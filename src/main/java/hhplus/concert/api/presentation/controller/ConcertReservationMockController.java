package hhplus.concert.api.presentation.controller;

import hhplus.concert.api.presentation.dto.ConcertScheduleInfo;
import hhplus.concert.api.presentation.dto.ConcertSeatInfo;
import hhplus.concert.api.presentation.dto.request.BalanceChargeRequest;
import hhplus.concert.api.presentation.dto.request.PaymentRequest;
import hhplus.concert.api.presentation.dto.request.ReservationRequest;
import hhplus.concert.api.presentation.dto.response.*;
import hhplus.concert.support.type.PaymentStatus;
import hhplus.concert.support.type.ReservationStatus;
import hhplus.concert.support.type.SeatStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/mock")
public class ConcertReservationMockController {
    /**
     * concert domain
     */
    // 특정 콘서트의 예약 가능한 일정 목록을 조회합니다.
    @GetMapping("/concerts/{concertId}/schedules")
    public ConcertScheduleResponse getConcertSchedules(
            @PathVariable Long concertId,
            @RequestHeader("Authorization") String token) {
        List<ConcertScheduleInfo> concertSchedules = List.of(
                new ConcertScheduleInfo(
                        1L,
                        LocalDateTime.now(),
                        LocalDateTime.now().minusDays(7)),
                new ConcertScheduleInfo(
                        2L,
                        LocalDateTime.now(),
                        LocalDateTime.now().minusDays(7))
        );
        return new ConcertScheduleResponse(
                concertId,
                concertSchedules);
    }

    // 해당 날짜의 콘서트 좌석 목록을 조회한다.
    @GetMapping("/concerts/{concertId}/schedules/{scheduleId}/seats")
    public ConcertSeatResponse getConcertSeats(
            @PathVariable Long concertId,
            @PathVariable Long scheduleId,
            @RequestHeader("Authorization") String token) {
        List<ConcertSeatInfo> concertSeats = List.of(
                new ConcertSeatInfo(
                        1L,
                        1,
                        SeatStatus.AVAILABLE, 10000),
                new ConcertSeatInfo(
                        2L,
                        2,
                        SeatStatus.UNAVAILABLE,
                        20000)
        );
        return new ConcertSeatResponse(
                concertId,
                LocalDateTime.now(),
                concertSeats);
    }

    // 해당 날짜에 진행되는 콘서트의 특정 좌석을 예약한다.
    @PostMapping("/concerts/reservations")
    public ReservationResponse createReservation(
            @RequestBody ReservationRequest reservationRequest,
            @RequestHeader("Authorization") String token) {
        return new ReservationResponse(
                1L,
                1L,
                "아이유 콘서트",
                LocalDateTime.now(),
                1,
                10000,
                ReservationStatus.PENDING
        );
    }

    /**
     * payment domain
     */
    // 콘서트 좌석 예약에 대한 결제를 진행합니다.
    @PostMapping("/payment/concerts/users/{userId}")
    public PaymentResponse executePayment(
            @PathVariable Long userId,
            @RequestBody PaymentRequest paymentRequest,
            @RequestHeader("Authorization") String token) {
        return new PaymentResponse(
                1L,
                10000L,
                PaymentStatus.COMPLETED
        );
    }

    /**
     * balance domain
     */
    // 잔액을 충전한다.
    @PostMapping("/balance/users/{userId}/charge")
    public BalanceResponse chargeBalance(
            @PathVariable Long userId,
            BalanceChargeRequest balanceRequest
    ){
        return new BalanceResponse(1L,40000L);
    }

    // 잔액을 조회한다.
    @GetMapping("/balance/users/{userId}")
    public BalanceResponse getUserBalance(
            @PathVariable Long userId
    ){
        return new BalanceResponse(1L,40000L);
    }
}