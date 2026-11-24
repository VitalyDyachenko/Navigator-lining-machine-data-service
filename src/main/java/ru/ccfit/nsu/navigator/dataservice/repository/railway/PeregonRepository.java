package ru.ccfit.nsu.navigator.dataservice.repository.railway;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.ccfit.nsu.navigator.dataservice.entity.railway.Peregon;
import java.util.Optional;

public interface PeregonRepository extends JpaRepository<Peregon, Long> {

    Optional<Peregon> findByDirection_IdAndSourceId(Long directionId, int sourceId);
}
