package ru.ccfit.nsu.navigator.dataservice.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "survey_switch")
public class SurveySwitch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "work_id", nullable = false)
    private Long workId;

    @Column(name = "step_index", nullable = false)
    private int stepIndex;

    @Column(name = "is_trail_direction")
    private Boolean trailDirection;

    @Column(name = "type")
    private short type;

    @Column(name = "length_m")
    private int lengthM;

    @Column(name = "switch_number")
    private String number;

    @Column(name = "station_code")
    private int stationCode;

    @Column(name = "is_passport", nullable = false)
    private boolean fromPassport;

    protected SurveySwitch() {
    }

    public SurveySwitch(Long workId, int stepIndex, Boolean trailDirection,
                        short type, int lengthM, String number,
                        int stationCode, boolean fromPassport) {
        this.workId = workId;
        this.stepIndex = stepIndex;
        this.trailDirection = trailDirection;
        this.type = type;
        this.lengthM = lengthM;
        this.number = number;
        this.stationCode = stationCode;
        this.fromPassport = fromPassport;
    }
}