
package ru.ccfit.nsu.navigator.dataservice.entity.work;

import jakarta.persistence.*;
import ru.ccfit.nsu.navigator.dataservice.entity.machine.Machine;
import ru.ccfit.nsu.navigator.dataservice.entity.railway.MainTrack;
import ru.ccfit.nsu.navigator.dataservice.entity.railway.Peregon;

@Entity
@Table(name = "work")
public class Work {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "track_number")
    private String trackNumber;

    @Column(name = "adm_code")
    private Short administrativeCode;

    @Column(name = "step_size_mm")
    private Integer stepSizeMm;

    @Column(name = "step_count")
    private Long stepCount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "machine_id")
    private Machine machine;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "main_track_id", nullable = false)
    private MainTrack mainTrack;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "peregon_id", nullable = false)
    private Peregon peregon;

    public void setMainTrack(MainTrack mainTrack) {
        this.mainTrack = mainTrack;
    }

    public void setPeregon(Peregon peregon) {
        this.peregon = peregon;
    }

    public void setMachine(Machine machine) {
        this.machine = machine;
    }

    protected Work() {
    }

    public static Work createPending() {
        return new Work();
    }

    public void setTrackInfo(
            String trackNumber,
            short administrativeCode
    ) {
        this.trackNumber = trackNumber;
        this.administrativeCode = administrativeCode;
    }

    public void setMeasurementInfo(
            int stepSizeMm,
            long stepCount
    ) {
        this.stepSizeMm = stepSizeMm;
        this.stepCount = stepCount;
    }

    public Long getId() {
        return id;
    }
}
