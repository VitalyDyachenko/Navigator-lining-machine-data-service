package ru.ccfit.nsu.navigator.dataservice.repository.railway;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.ccfit.nsu.navigator.dataservice.entity.railway.Direction;
import java.util.List;

public interface DirectionRepository extends JpaRepository<Direction, Long> {

    List<Direction> findAllByRailway_IdAndSourceId(Long railwayId, int sourceId);
}