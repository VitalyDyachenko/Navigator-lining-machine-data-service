
package ru.ccfit.nsu.navigator.dataservice.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "survey_gabarit")
public class SurveyGabarit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "work_id", nullable = false)
    private Long workId;

    @Column(name = "control_distance_m")
    private double controlDistanceM;

    @Column(name = "step_index")
    private int stepIndex;

    @Column(name = "kilometer")
    private int kilometer;

    @Column(name = "picket")
    private short picket;

    @Column(name = "meter")
    private float meter;

    @Column(name = "object_type")
    private short objectType;

    @Column(name = "clearance_code")
    private short clearanceCode;

    @Column(name = "intertrack_code")
    private short intertrackCode;

    @Column(name = "left_distance_mm")
    private int leftDistanceMm;

    @Column(name = "right_distance_mm")
    private int rightDistanceMm;

    @Column(name = "height_mm")
    private short heightMm;

    @Column(name = "left_range_mm")
    private short leftRangeMm;

    @Column(name = "right_range_mm")
    private short rightRangeMm;

    @Column(name = "description")
    private String description;

    protected SurveyGabarit() {
    }

    public SurveyGabarit(Long workId, double controlDistanceM, int stepIndex,
                         int kilometer, short picket, float meter, short objectType,
                         short clearanceCode, short intertrackCode,
                         int leftDistanceMm, int rightDistanceMm, short heightMm,
                         short leftRangeMm, short rightRangeMm, String description) {
        this.workId = workId;
        this.controlDistanceM = controlDistanceM;
        this.stepIndex = stepIndex;
        this.kilometer = kilometer;
        this.picket = picket;
        this.meter = meter;
        this.objectType = objectType;
        this.clearanceCode = clearanceCode;
        this.intertrackCode = intertrackCode;
        this.leftDistanceMm = leftDistanceMm;
        this.rightDistanceMm = rightDistanceMm;
        this.heightMm = heightMm;
        this.leftRangeMm = leftRangeMm;
        this.rightRangeMm = rightRangeMm;
        this.description = description;
    }
}
