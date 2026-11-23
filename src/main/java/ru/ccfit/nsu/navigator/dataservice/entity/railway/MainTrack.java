package ru.ccfit.nsu.navigator.dataservice.entity.railway;

import jakarta.persistence.*;

@Entity
@Table(name = "main_track")
public class MainTrack {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "source_id", nullable = false)
    private int sourceId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "direction_id", nullable = false)
    private Direction direction;

    @Column(name = "number", length = 7)
    private String number;

    @Column(name = "gauge_width_id")
    private short gaugeWidthId;

    @Column(name = "main_track_source_id_osn")
    private int mainTrackSourceIdOsn;

    protected MainTrack() {
    }

    public MainTrack(int sourceId, Direction direction, String number,
                     short gaugeWidthId, int mainTrackSourceIdOsn) {
        this.sourceId = sourceId;
        this.direction = direction;
        this.number = number;
        this.gaugeWidthId = gaugeWidthId;
        this.mainTrackSourceIdOsn = mainTrackSourceIdOsn;
    }

    public Long getId() {
        return id;
    }
}
