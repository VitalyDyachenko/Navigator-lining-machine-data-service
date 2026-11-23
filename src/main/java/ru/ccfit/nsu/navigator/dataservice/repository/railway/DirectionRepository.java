package ru.ccfit.nsu.navigator.dataservice.repository.railway;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.ccfit.nsu.navigator.dataservice.entity.railway.Direction;
import java.util.Optional;

public interface DirectionRepository extends JpaRepository<Direction, Long> {

    Optional<Direction> findByRailway_IdAndSourceId(Long railwayId, int sourceId);
}