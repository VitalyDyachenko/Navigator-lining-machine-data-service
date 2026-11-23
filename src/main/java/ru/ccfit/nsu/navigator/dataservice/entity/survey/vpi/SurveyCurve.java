
package ru.ccfit.nsu.navigator.dataservice.entity.survey.vpi;

import jakarta.persistence.*;

@Entity
@Table(name = "survey_curve")
public class SurveyCurve {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "work_id", nullable = false)
    private Long workId;

    @Column(name = "step_index", nullable = false)
    private int stepIndex;

    @Column(name = "radius_m", nullable = false)
    private float radiusM;

    @Column(name = "gauge_norm_mm", nullable = false)
    private float gaugeNormMm;

    @Column(name = "wear_mm", nullable = false)
    private float wearMm;

    @Column(name = "tolerance_mm", nullable = false)
    private float toleranceMm;

    protected SurveyCurve() {
    }

    public SurveyCurve(Long workId, int stepIndex, float radiusM,
                       float gaugeNormMm, float wearMm, float toleranceMm) {
        this.workId = workId;
        this.stepIndex = stepIndex;
        this.radiusM = radiusM;
        this.gaugeNormMm = gaugeNormMm;
        this.wearMm = wearMm;
        this.toleranceMm = toleranceMm;
    }
}
