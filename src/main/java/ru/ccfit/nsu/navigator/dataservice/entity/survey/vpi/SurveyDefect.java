
package ru.ccfit.nsu.navigator.dataservice.entity.survey.vpi;

import jakarta.persistence.*;

@Entity
@Table(name = "survey_defect")
public class SurveyDefect {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "work_id", nullable = false)
    private Long workId;

    @Column(name = "start_step_index", nullable = false)
    private int startStepIndex;

    @Column(name = "end_step_index", nullable = false)
    private int endStepIndex;

    @Column(name = "mask", nullable = false)
    private int mask;

    @Column(name = "value_mm", nullable = false)
    private float valueMm;

    @Column(name = "type_code", nullable = false)
    private int typeCode;

    @Column(name = "passenger_speed", nullable = false)
    private int passengerSpeed;

    @Column(name = "freight_speed", nullable = false)
    private int freightSpeed;

    @Column(name = "empty_train_speed", nullable = false)
    private int emptyTrainSpeed;

    @Column(name = "degree", nullable = false)
    private short degree;

    @Column(name = "label")
    private String label;

    protected SurveyDefect() {
    }

    public SurveyDefect(Long workId, int startStepIndex, int endStepIndex,
                        int mask, float valueMm, int typeCode,
                        int passengerSpeed, int freightSpeed,
                        int emptyTrainSpeed, short degree, String label) {
        this.workId = workId;
        this.startStepIndex = startStepIndex;
        this.endStepIndex = endStepIndex;
        this.mask = mask;
        this.valueMm = valueMm;
        this.typeCode = typeCode;
        this.passengerSpeed = passengerSpeed;
        this.freightSpeed = freightSpeed;
        this.emptyTrainSpeed = emptyTrainSpeed;
        this.degree = degree;
        this.label = label;
    }
}
