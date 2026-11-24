package ru.ccfit.nsu.navigator.dataservice.parser.srv.model.rfid;


public record SrvRfid(
        int kilometer,
        int picket,
        int markerType,
        long syncPulseCount,
        long tagIdentifier,
        long[] dataBlocks,
        int blockMask,
        int pointCount,
        boolean hasError,
        double controlDistanceM
) {}
