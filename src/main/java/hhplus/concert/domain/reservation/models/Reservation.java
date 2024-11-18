package hhplus.concert.domain.reservation.models;

import hhplus.concert.domain.concert.models.Concert;
import hhplus.concert.domain.concert.models.Seat;
import hhplus.concert.domain.user.models.User;
import hhplus.concert.support.type.ReservationStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "reservation", indexes = {
        @Index(name = "idx_status_reservation_at", columnList = "status, reservation_at")
})
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reservation_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private User user;

    @ManyToOne
    @JoinColumn(name = "concert_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Concert concert;

    @ManyToOne
    @JoinColumn(name = "seat_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Seat seat;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    @Column(name = "reservation_at", nullable = false)
    private LocalDateTime reservationAt;

    public Reservation(User user, Concert concert, Seat seat, ReservationStatus status, LocalDateTime reservationAt) {
        this.user = user;
        this.concert = concert;
        this.seat = seat;
        this.status = status;
        this.reservationAt = reservationAt;
    }

    public void updateStatus(ReservationStatus reservationStatus) {
        this.status = reservationStatus;
    }
}
