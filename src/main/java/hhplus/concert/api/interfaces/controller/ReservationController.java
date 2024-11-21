package hhplus.concert.api.interfaces.controller;

import hhplus.concert.api.interfaces.request.ReservationRequest;
import hhplus.concert.api.interfaces.response.ReservationResponse;
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
            @RequestHeader("Token") String token) {
        return ReservationResponse.Result.from(reservationFacade.createReservation(ReservationRequest.toDto(reservationRequest), token));
    }
}
