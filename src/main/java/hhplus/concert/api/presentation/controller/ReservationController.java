package hhplus.concert.api.presentation.controller;

import hhplus.concert.api.presentation.request.ReservationRequest;
import hhplus.concert.api.presentation.response.ReservationResponse;
import hhplus.concert.application.reservation.usecase.ReservationFacade;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationFacade reservationFacade;

    public ReservationController(ReservationFacade reservationFacade) {
        this.reservationFacade = reservationFacade;
    }

    // 해당 날짜에 진행되는 콘서트의 특정 좌석을 예약한다.
    @PostMapping("/concerts")
    public ReservationResponse.Result createReservation(
            @RequestBody ReservationRequest.Detail reservationRequest,
            @RequestHeader("TOKEN") String token) {
        return ReservationResponse.Result.from(reservationFacade.createReservation(ReservationRequest.toDto(reservationRequest), token));
    }
}
