package ru.ccfit.nsu.navigator.dataservice.repository.railway;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.ccfit.nsu.navigator.dataservice.entity.railway.StationPosition;
import ru.ccfit.nsu.navigator.dataservice.entity.railway.StationPositionId;

public interface StationPositionRepository
        extends JpaRepository<StationPosition, StationPositionId> {
}
