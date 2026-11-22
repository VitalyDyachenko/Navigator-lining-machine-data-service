
package ru.ccfit.nsu.navigator.dataservice.entity;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "direction")
public class Direction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "source_id", nullable = false)
    private int sourceId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "railway_id", nullable = false)
    private Railway railway;

    @Column(name = "code")
    private short code;

    @Column(name = "mag_code")
    private short magCode;

    @Column(name = "name", length = 50)
    private String name;

    @Column(name = "start_station_source_id")
    private int startStationSourceId;

    @Column(name = "end_station_source_id")
    private int endStationSourceId;

    protected Direction() {
    }

    public Direction(int sourceId, Railway railway, short code, short magCode,
                     String name, int startStationSourceId, int endStationSourceId) {
        this.sourceId = sourceId;
        this.railway = railway;
        this.code = code;
        this.magCode = magCode;
        this.name = name;
        this.startStationSourceId = startStationSourceId;
        this.endStationSourceId = endStationSourceId;
    }

    public boolean hasSameData(Direction other) {
        return code == other.code
                && magCode == other.magCode
                && startStationSourceId == other.startStationSourceId
                && endStationSourceId == other.endStationSourceId
                && Objects.equals(name, other.name);
    }

    public Long getId() {
        return id;
    }
}
