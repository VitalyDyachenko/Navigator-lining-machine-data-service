
package ru.ccfit.nsu.navigator.dataservice.entity.survey.vpi;

import jakarta.persistence.*;

@Entity
@Table(name = "survey_fragment")
public class SurveyFragment {

    @Id
    @Column(name = "work_id")
    private Long workId;

    @Column(name = "start_step_index")
    private int startStepIndex;

    @Column(name = "start_km")
    private short startKm;

    @Column(name = "start_m")
    private short startM;

    @Column(name = "end_km")
    private short endKm;

    @Column(name = "end_m")
    private short endM;

    @Column(name = "direction_station_code")
    private long directionStationCode;

    @Column(name = "park_code")
    private short parkCode;

    @Column(name = "track_number")
    private String trackNumber;

    @Column(name = "against_picketage")
    private boolean againstPicketage;

    @Column(name = "name")
    private String name;

    protected SurveyFragment() {
    }

    public SurveyFragment(Long workId, int startStepIndex, short startKm, short startM,
                          short endKm, short endM, long directionStationCode, short parkCode,
                          String trackNumber, boolean againstPicketage, String name) {
        this.workId = workId;
        this.startStepIndex = startStepIndex;
        this.startKm = startKm;
        this.startM = startM;
        this.endKm = endKm;
        this.endM = endM;
        this.directionStationCode = directionStationCode;
        this.parkCode = parkCode;
        this.trackNumber = trackNumber;
        this.againstPicketage = againstPicketage;
        this.name = name;
    }
}
