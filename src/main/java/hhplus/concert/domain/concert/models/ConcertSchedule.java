package hhplus.concert.domain.concert.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "concert_schedule", indexes = @Index(name = "idx_concert_id", columnList = "concert_id"))
public class ConcertSchedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "concert_schedule_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "concert_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Concert concert;

    @Column(name = "concert_at", nullable = false)
    private LocalDateTime concertAt;

    @Column(name = "reservation_at", nullable = false)
    private LocalDateTime reservationAt;

    public ConcertSchedule(Concert concert, LocalDateTime concertAt, LocalDateTime reservationAt) {
        this.concert = concert;
        this.concertAt = concertAt;
        this.reservationAt = reservationAt;
    }
}
