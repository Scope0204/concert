package hhplus.concert.domain.entity;

import jakarta.persistence.*;

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
