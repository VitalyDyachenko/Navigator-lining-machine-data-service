
package ru.ccfit.nsu.navigator.dataservice.entity.railway;

import jakarta.persistence.*;

@Entity
@Table(name = "station_position")
@IdClass(StationPositionId.class)
public class StationPosition {

    @Id
    @Column(name = "work_id", nullable = false)
    private Long workId;

    @Id
    @Column(name = "station_order", nullable = false)
    private Short stationOrder;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "station_id", nullable = false)
    private Station station;

    @Column(name = "axis_attribute_id")
    private Short axisAttributeId;

    @Column(name = "axis_km")
    private Integer axisKm;

    @Column(name = "axis_m")
    private Short axisM;

    @Column(name = "start_km")
    private Integer startKm;

    @Column(name = "start_m")
    private Short startM;

    @Column(name = "end_km")
    private Integer endKm;

    @Column(name = "end_m")
    private Short endM;

    protected StationPosition() {
    }

    public StationPosition(
            Long workId,
            short stationOrder,
            Station station,
            short axisAttributeId,
            int axisKm,
            short axisM,
            int startKm,
            short startM,
            int endKm,
            short endM
    ) {
        this.workId = workId;
        this.stationOrder = stationOrder;
        this.station = station;
        this.axisAttributeId = axisAttributeId;
        this.axisKm = axisKm;
        this.axisM = axisM;
        this.startKm = startKm;
        this.startM = startM;
        this.endKm = endKm;
        this.endM = endM;
    }
}
