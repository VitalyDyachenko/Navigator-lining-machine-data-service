
package ru.ccfit.nsu.navigator.dataservice.entity.survey;

import jakarta.persistence.*;
import ru.ccfit.nsu.navigator.dataservice.entity.machine.MachineParams;

import java.time.LocalDateTime;

@Entity
@Table(name = "survey")
public class Survey {

    @Id
    @Column(name = "work_id")
    private Long workId;

    @Column(name = "version", nullable = false)
    private short version;

    @Column(name = "survey_time")
    private LocalDateTime surveyTime;

    @Column(name = "channel_mask", nullable = false)
    private int channelMask;

    @Column(name = "start_km")
    private Integer startKm;

    @Column(name = "start_m")
    private Short startM;

    @Column(name = "start_mm")
    private Short startMm;

    @Column(name = "end_km")
    private Integer endKm;

    @Column(name = "end_m")
    private Short endM;

    @Column(name = "end_mm")
    private Short endMm;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "machine_params_id", nullable = false)
    private MachineParams machineParams;


    @Column(name = "against_picketage")
    private Boolean againstPicketage;

    @Column(name = "moving_backward")
    private Boolean movingBackward;

    @Column(name = "left_press")
    private Boolean leftPress;

    @Column(name = "right_press")
    private Boolean rightPress;

    @Column(name = "short_chord")
    private Boolean shortChord;

    @Column(name = "intertrack_space")
    private Short intertrackSpace;

    @Column(name = "marker_at_front")
    private Boolean markerAtFront;

    @Column(name = "dls_enabled")
    private Boolean dlsEnabled;

    @Column(name = "vpi_start_stp")
    private Long vpiStartStep;

    @Column(name = "trip_number")
    private Integer tripNumber;

    @Column(name = "trip_wagon_number")
    private Short tripWagonNumber;

    @Column(name = "trip_year")
    private Short tripYear;

    @Column(name = "trip_month")
    private Short tripMonth;

    @Column(name = "trip_railway_code")
    private Short tripRailwayCode;

    @Column(name = "trip_check_code")
    private Short tripCheckCode;

    @Column(name = "trip_checked")
    private Boolean tripChecked;

    protected Survey() {
    }

    public Survey(Long workId, MachineParams machineParams,
                  short version, LocalDateTime surveyTime, int channelMask) {
        this.workId = workId;
        this.machineParams = machineParams;
        this.version = version;
        this.surveyTime = surveyTime;
        this.channelMask = channelMask;
    }

    public void setFlags(boolean againstPicketage, boolean movingBackward,
                         boolean leftPress, boolean rightPress, boolean shortChord,
                         short intertrackSpace, boolean markerAtFront, boolean dlsEnabled) {
        this.againstPicketage = againstPicketage;
        this.movingBackward = movingBackward;
        this.leftPress = leftPress;
        this.rightPress = rightPress;
        this.shortChord = shortChord;
        this.intertrackSpace = intertrackSpace;
        this.markerAtFront = markerAtFront;
        this.dlsEnabled = dlsEnabled;
    }

    public void setTrip(long startStep, int number, short wagonNumber, short year,
                        short month, short railwayCode, short checkCode, boolean checked) {
        this.vpiStartStep = startStep;
        this.tripNumber = number;
        this.tripWagonNumber = wagonNumber;
        this.tripYear = year;
        this.tripMonth = month;
        this.tripRailwayCode = railwayCode;
        this.tripCheckCode = checkCode;
        this.tripChecked = checked;
    }

    public void setTrackRange(
            int startKm, short startM, short startMm,
            int endKm, short endM, short endMm
    ) {
        this.startKm = startKm;
        this.startM = startM;
        this.startMm = startMm;
        this.endKm = endKm;
        this.endM = endM;
        this.endMm = endMm;
    }

    public Integer getStartKm() {
        return startKm;
    }

    public Short getStartM() {
        return startM;
    }

    public Short getStartMm() {
        return startMm;
    }

    public Integer getEndKm() {
        return endKm;
    }

    public Short getEndM() {
        return endM;
    }

    public Short getEndMm() {
        return endMm;
    }
}
