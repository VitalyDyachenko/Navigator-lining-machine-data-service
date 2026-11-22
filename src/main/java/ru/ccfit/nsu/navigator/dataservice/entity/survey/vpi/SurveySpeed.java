package ru.ccfit.nsu.navigator.dataservice.entity.survey.vpi;

import jakarta.persistence.*;

@Entity
@Table(name = "survey_speed")
public class SurveySpeed {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "work_id", nullable = false)
    private Long workId;

    @Column(name = "start_step_index", nullable = false)
    private int startStepIndex;

    @Column(name = "end_step_index", nullable = false)
    private int endStepIndex;

    @Column(name = "start_km", nullable = false)
    private int startKm;

    @Column(name = "start_picket", nullable = false)
    private short startPicket;

    @Column(name = "end_km", nullable = false)
    private int endKm;

    @Column(name = "end_picket", nullable = false)
    private short endPicket;

    @Column(name = "passenger_speed", nullable = false)
    private int passengerSpeed;

    @Column(name = "freight_speed", nullable = false)
    private int freightSpeed;

    @Column(name = "empty_train_speed", nullable = false)
    private int emptyTrainSpeed;

    protected SurveySpeed() {
    }

    public SurveySpeed(Long workId, int startStepIndex, int endStepIndex,
                       int startKm, short startPicket, int endKm, short endPicket,
                       int passengerSpeed, int freightSpeed, int emptyTrainSpeed) {
        this.workId = workId;
        this.startStepIndex = startStepIndex;
        this.endStepIndex = endStepIndex;
        this.startKm = startKm;
        this.startPicket = startPicket;
        this.endKm = endKm;
        this.endPicket = endPicket;
        this.passengerSpeed = passengerSpeed;
        this.freightSpeed = freightSpeed;
        this.emptyTrainSpeed = emptyTrainSpeed;
    }
}