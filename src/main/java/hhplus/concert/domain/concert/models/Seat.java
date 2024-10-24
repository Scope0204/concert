package hhplus.concert.domain.concert.models;

import hhplus.concert.support.type.SeatStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seat_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "concert_schedule_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private ConcertSchedule concertSchedule;

    @Column(name = "seat_number", nullable = false)
    private int seatNumber;

    @Column(name = "seat_price", nullable = false)
    private int seatPrice;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private SeatStatus status;

    public Seat(ConcertSchedule concertSchedule, int seatNumber, int seatPrice, SeatStatus status) {
        this.concertSchedule = concertSchedule;
        this.seatNumber = seatNumber;
        this.seatPrice = seatPrice;
        this.status = status;
    }

}
