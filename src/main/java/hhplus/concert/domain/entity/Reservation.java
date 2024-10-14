package hhplus.concert.domain.entity;

import hhplus.concert.common.type.SeatStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reservation_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private User user;

    @ManyToOne
    @JoinColumn(name = "seat_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Seat seat;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private SeatStatus status;

    @Column(name = "reservation_at", nullable = false)
    private LocalDateTime reservationAt;

    public Reservation(User user, Seat seat, SeatStatus status, LocalDateTime reservationAt) {
        this.user = user;
        this.seat = seat;
        this.status = status;
        this.reservationAt = reservationAt;
    }
}
