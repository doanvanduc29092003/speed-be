package org.example.speeded.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "runs")
@Getter
@Setter

public class Run {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double distance;
    private Integer time;
    private Double calories;

    @Column(columnDefinition = "TEXT")
    private String route;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    // getter/setter
}