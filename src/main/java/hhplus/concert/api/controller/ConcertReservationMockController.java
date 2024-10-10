package hhplus.concert.api.controller;

import hhplus.concert.api.dto.ConcertScheduleInfo;
import hhplus.concert.api.dto.ConcertSeatInfo;
import hhplus.concert.api.dto.request.BalanceChargeRequest;
import hhplus.concert.api.dto.request.PaymentRequest;
import hhplus.concert.api.dto.request.ReservationRequest;
import hhplus.concert.api.dto.response.*;
import hhplus.concert.common.type.PaymentStatus;
import hhplus.concert.common.type.QueueStatus;
import hhplus.concert.common.type.ReservationStatus;
import hhplus.concert.common.type.SeatStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/mock")
public class ConcertReservationMockController {
    /**
     * queue domain
     */
    // 대기열에 사용자를 추가하고 대기열 토큰을 반환하도록 합니다.
    @PostMapping("/queue/token/users/{userId}")
    public QueueTokenResponse issueQueueToken(
            @PathVariable Long userId) {
        return new QueueTokenResponse(
                1L,
                LocalDateTime.now(),
                LocalDateTime.now().plusMinutes(5)
        );
    }
    // 사용자에게 발급된 토큰 정보를 조회합니다.
    @GetMapping("/queue/token/users/{userId}")
    public QueueTokenResponse getWaitingToken(
            @PathVariable Long userId) {
        return new QueueTokenResponse(
                1L,
                LocalDateTime.now(),
                LocalDateTime.now().plusMinutes(5)
        );
    }
    // 유저 토큰을 통해 사용자의 대기열 상태를 조회합니다.
    @GetMapping("/queue/status")
    public QueueResponse getQueueStatus(
            @RequestHeader("Authorization") String token) {
        return new QueueResponse(
                1L,
                QueueStatus.WAIT,
                LocalDateTime.now()
        );
    }

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