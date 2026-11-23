package ru.ccfit.nsu.navigator.dataservice.entity.railway;

import java.io.Serializable;
import java.util.Objects;

public class StationPositionId implements Serializable {

    private Long workId;
    private Short stationOrder;

    public StationPositionId() {
    }

    public StationPositionId(Long workId, Short stationOrder) {
        this.workId = workId;
        this.stationOrder = stationOrder;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof StationPositionId other)) return false;

        return Objects.equals(workId, other.workId)
                && Objects.equals(stationOrder, other.stationOrder);
    }

    @Override
    public int hashCode() {
        return Objects.hash(workId, stationOrder);
    }
}
