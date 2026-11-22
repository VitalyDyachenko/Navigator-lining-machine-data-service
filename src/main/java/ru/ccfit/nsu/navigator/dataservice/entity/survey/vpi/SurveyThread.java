package ru.ccfit.nsu.navigator.dataservice.entity.survey.vpi;

import jakarta.persistence.*;

@Entity
@Table(name = "survey_thread")
public class SurveyThread {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "work_id", nullable = false)
    private Long workId;

    @Column(name = "start_step_index", nullable = false)
    private int startStepIndex;

    @Column(name = "end_step_index", nullable = false)
    private int endStepIndex;

    protected SurveyThread() {
    }

    public SurveyThread(Long workId, int startStepIndex, int endStepIndex) {
        this.workId = workId;
        this.startStepIndex = startStepIndex;
        this.endStepIndex = endStepIndex;
    }
}