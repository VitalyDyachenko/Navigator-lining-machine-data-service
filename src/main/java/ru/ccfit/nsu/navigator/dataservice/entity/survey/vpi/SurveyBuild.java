package ru.ccfit.nsu.navigator.dataservice.entity.survey.vpi;

import jakarta.persistence.*;

@Entity
@Table(name = "survey_build")
public class SurveyBuild {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "work_id", nullable = false)
    private Long workId;

    @Column(name = "start_step_index", nullable = false)
    private int startStepIndex;

    @Column(name = "end_step_index", nullable = false)
    private int endStepIndex;

    @Column(name = "type_id", nullable = false)
    private short typeId;

    @Column(name = "is_passport", nullable = false)
    private boolean fromPassport;

    protected SurveyBuild() {
    }

    public SurveyBuild(Long workId, int startStepIndex, int endStepIndex,
                       short typeId, boolean fromPassport) {
        this.workId = workId;
        this.startStepIndex = startStepIndex;
        this.endStepIndex = endStepIndex;
        this.typeId = typeId;
        this.fromPassport = fromPassport;
    }
}