
package ru.ccfit.nsu.navigator.dataservice.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "survey_urov")
public class SurveyElevation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "work_id", nullable = false)
    private Long workId;

    @Column(name = "step_index", nullable = false)
    private int stepIndex;

    @Column(name = "value_mm", nullable = false)
    private float valueMm;

    protected SurveyElevation() {
    }

    public SurveyElevation(Long workId, int stepIndex, float valueMm) {
        this.workId = workId;
        this.stepIndex = stepIndex;
        this.valueMm = valueMm;
    }
}
