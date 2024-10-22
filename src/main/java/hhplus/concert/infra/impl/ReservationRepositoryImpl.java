package hhplus.concert.infra.impl;


import hhplus.concert.domain.reservation.models.Reservation;
import hhplus.concert.domain.reservation.repositories.ReservationRepository;
import hhplus.concert.infra.jpa.JpaReservationRepository;
import hhplus.concert.support.error.ErrorCode;
import hhplus.concert.support.error.exception.BusinessException;
import hhplus.concert.support.type.ReservationStatus;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public class ReservationRepositoryImpl implements ReservationRepository {
    private final JpaReservationRepository jpaReservationRepository;

    public ReservationRepositoryImpl(JpaReservationRepository jpaReservationRepository) {
        this.jpaReservationRepository = jpaReservationRepository;
    }

    @Override
    public void save(Reservation reservation) {
        jpaReservationRepository.save(reservation);
    }

    @Override
    public Reservation findById(Long reservationId) {
        return jpaReservationRepository.findById(reservationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESERVATION_NOT_FOUND));
    }

    @Override
    public List<Reservation> findAll() {
        return jpaReservationRepository.findAll();
    }

    @Override
    public List<Reservation> findExpiredReservations(ReservationStatus reservationStatus, LocalDateTime expirationTime) {
        return jpaReservationRepository.findExpiredReservations(reservationStatus, expirationTime);
    }

    @Override
    public void updateStatus(Long reservationId, ReservationStatus reservationStatus) {
        jpaReservationRepository.updateStatus(reservationId, reservationStatus);
    }

    @Override
    public void updateAllStatus(List<Long> reservationIds, ReservationStatus reservationStatus) {
        jpaReservationRepository.updateAllStatus(reservationIds, reservationStatus);
    }
}
