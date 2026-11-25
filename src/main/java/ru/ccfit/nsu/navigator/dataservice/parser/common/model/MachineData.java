package ru.ccfit.nsu.navigator.dataservice.parser.common.model;

public record MachineData(
        int rawType,
        int machineType,
        String name,
        long number,

        boolean continuousCorrection,
        boolean profileEnabled,
        boolean planeSatellite,
        int tampingSleepers,

        int[] alignmentArms,
        int[] levelingArms,
        int[] trolleyArms,
        int gaugeWidth
) {
}