package hhplus.concert.domain.concert.models;

import hhplus.concert.support.type.ConcertStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
public class Concert {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "concert_id")
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private ConcertStatus concertStatus;

    public Concert(String title, String description, ConcertStatus concertStatus) {
        this.title = title;
        this.description = description;
        this.concertStatus = concertStatus;
    }
}
