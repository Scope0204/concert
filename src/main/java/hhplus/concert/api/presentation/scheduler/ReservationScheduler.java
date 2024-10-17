package hhplus.concert.api.presentation.scheduler;

import hhplus.concert.application.reservation.usecase.ReservationFacade;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ReservationScheduler {
    private final ReservationFacade reservationFacade;

    public ReservationScheduler(ReservationFacade reservationFacade) {
        this.reservationFacade = reservationFacade;
    }

    @Scheduled(fixedDelay = 60000)
    public void reservationFacade(){
        reservationFacade.cancelReservationsAndResetSeats();
    }
}
