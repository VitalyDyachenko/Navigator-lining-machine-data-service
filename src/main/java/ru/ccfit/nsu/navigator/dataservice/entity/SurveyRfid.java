package ru.ccfit.nsu.navigator.dataservice.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "survey_rfid")
public class SurveyRfid {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "work_id", nullable = false)
    private Long workId;

    @Column(name = "sync_pulse_count", nullable = false)
    private long syncPulseCount;

    @Column(name = "tag_identifier", nullable = false)
    private long tagIdentifier;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "data_blocks", nullable = false, columnDefinition = "bigint[]")
    private long[] dataBlocks;

    @Column(name = "kilometer", nullable = false)
    private int kilometer;

    @Column(name = "picket", nullable = false)
    private short picket;

    @Column(name = "marker_type_id")
    private Short markerTypeId;

    @Column(name = "block_mask", nullable = false)
    private short blockMask;

    @Column(name = "point_count", nullable = false)
    private short pointCount;

    @Column(name = "has_error", nullable = false)
    private boolean hasError;

    @Column(name = "control_distance_m", nullable = false)
    private double controlDistanceM;

    protected SurveyRfid() {
    }

    public SurveyRfid(Long workId, long syncPulseCount, long tagIdentifier,
                      long[] dataBlocks, int kilometer, short picket,
                      short markerTypeId, short blockMask, short pointCount,
                      boolean hasError, double controlDistanceM) {
        this.workId = workId;
        this.syncPulseCount = syncPulseCount;
        this.tagIdentifier = tagIdentifier;
        this.dataBlocks = dataBlocks.clone();
        this.kilometer = kilometer;
        this.picket = picket;
        this.markerTypeId = markerTypeId;
        this.blockMask = blockMask;
        this.pointCount = pointCount;
        this.hasError = hasError;
        this.controlDistanceM = controlDistanceM;
    }
}
