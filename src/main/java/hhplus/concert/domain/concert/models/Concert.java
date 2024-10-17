package hhplus.concert.domain.concert.models;

import jakarta.persistence.*;
import lombok.Getter;

@Getter
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

    public Concert(String title, String description) {
        this.title = title;
        this.description = description;
    }
}
