package hhplus.concert.infra.jpa;

import hhplus.concert.domain.reservation.models.Reservation;
import hhplus.concert.support.type.ReservationStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface JpaReservationRepository extends JpaRepository<Reservation, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Reservation r WHERE r.id = :reservationId")
    Optional<Reservation> findByIdWithPessimisticLock(@Param("reservationId") Long reservationId);

    Optional<Reservation> findById(Long reservationId);

    @Query("SELECT r FROM Reservation r WHERE r.status = :reservationStatus AND r.reservationAt < :expirationTime")
    List<Reservation> findExpiredReservations(
            @Param("reservationStatus") ReservationStatus reservationStatus,
            @Param("expirationTime") LocalDateTime expirationTime
    );

    @Modifying
    @Query("UPDATE Reservation r SET r.status = :reservationStatus WHERE r.id = :reservationId")
    void updateStatus(
            @Param("reservationId") Long reservationId,
            @Param("reservationStatus") ReservationStatus reservationStatus
    );

    @Modifying
    @Query("UPDATE Reservation r SET r.status = :reservationStatus WHERE r.id IN :reservationIds")
    void updateAllStatus(
            @Param("reservationIds") List<Long> reservationIds,
            @Param("reservationStatus") ReservationStatus reservationStatus
    );

}

