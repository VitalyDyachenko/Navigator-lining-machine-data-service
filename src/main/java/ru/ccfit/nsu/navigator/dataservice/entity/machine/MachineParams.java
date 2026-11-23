package ru.ccfit.nsu.navigator.dataservice.entity.machine;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Arrays;

@Entity
@Table(name = "machine_params")
public class MachineParams {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "machine_id", nullable = false)
    private Machine machine;

    @Column(name = "continuous_correction")
    private boolean continuousCorrection;

    @Column(name = "profile_enabled")
    private boolean profileEnabled;

    @Column(name = "plane_satellite")
    private boolean planeSatellite;

    @Column(name = "tamping_sleepers")
    private Short tampingSleepers;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "alignment_arms_mm", nullable = false,
            columnDefinition = "integer[]")
    private int[] alignmentArmsMm;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "leveling_arms_mm", nullable = false,
            columnDefinition = "integer[]")
    private int[] levelingArmsMm;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "trolley_arms_mm", nullable = false,
            columnDefinition = "integer[]")
    private int[] trolleyArmsMm;

    @Column(name = "gauge_width_mm", nullable = false)
    private int gaugeWidthMm;

    protected MachineParams() {
    }

    public MachineParams(
            Machine machine,
            boolean continuousCorrection,
            boolean profileEnabled,
            boolean planeSatellite,
            int tampingSleepers,
            int[] alignmentArmsMm,
            int[] levelingArmsMm,
            int[] trolleyArmsMm,
            int gaugeWidthMm
    ) {
        this.machine = machine;
        this.continuousCorrection = continuousCorrection;
        this.profileEnabled = profileEnabled;
        this.planeSatellite = planeSatellite;
        this.tampingSleepers = (short) tampingSleepers;

        this.alignmentArmsMm =
                alignmentArmsMm.clone();

        this.levelingArmsMm =
                levelingArmsMm.clone();

        this.trolleyArmsMm =
                trolleyArmsMm.clone();

        this.gaugeWidthMm = gaugeWidthMm;
    }

    public boolean hasSameSettings(MachineParams other) {
        return continuousCorrection == other.continuousCorrection
                && profileEnabled == other.profileEnabled
                && planeSatellite == other.planeSatellite
                && tampingSleepers.equals(other.tampingSleepers)
                && gaugeWidthMm == other.gaugeWidthMm
                && Arrays.equals(alignmentArmsMm, other.alignmentArmsMm)
                && Arrays.equals(levelingArmsMm, other.levelingArmsMm)
                && Arrays.equals(trolleyArmsMm, other.trolleyArmsMm);
    }

    public Long getId() {
        return id;
    }
}
