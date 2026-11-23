package ru.ccfit.nsu.navigator.dataservice.entity.railway;

import jakarta.persistence.*;

@Entity
@Table(name = "peregon")
public class Peregon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "source_id", nullable = false)
    private int sourceId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "direction_id", nullable = false)
    private Direction direction;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "start_station_id", nullable = false)
    private Station startStation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "end_station_id", nullable = false)
    private Station endStation;

    @Column(name = "exploitation_length")
    private double exploitationLength;

    @Column(name = "name", length = 64)
    private String name;

    protected Peregon() {
    }

    public Peregon(int sourceId, Direction direction, Station startStation,
                   Station endStation, double exploitationLength, String name) {
        this.sourceId = sourceId;
        this.direction = direction;
        this.startStation = startStation;
        this.endStation = endStation;
        this.exploitationLength = exploitationLength;
        this.name = name;
    }

    public Long getId() {
        return id;
    }
}
