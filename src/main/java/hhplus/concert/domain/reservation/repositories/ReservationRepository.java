package hhplus.concert.domain.reservation.repositories;

import hhplus.concert.domain.reservation.models.Reservation;
import hhplus.concert.support.type.ReservationStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface ReservationRepository {
    void save(Reservation reservation);

    List<Reservation> findAll();

    List<Reservation> findExpiredReservations(
            ReservationStatus reservationStatus,
            LocalDateTime expirationTime
    );

    void updateAllStatus(
            List<Long> reservationIds,
            ReservationStatus reservationStatus
    );
}
