package ru.ccfit.nsu.navigator.dataservice.parser.srv.model;

public record SrvMachine(
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