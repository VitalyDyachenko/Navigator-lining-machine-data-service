package ru.ccfit.nsu.navigator.dataservice.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "work")
public class Work {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    protected Work() {
    }

    public static Work createPending() {
        return new Work();
    }

    public Long getId() {
        return id;
    }
}