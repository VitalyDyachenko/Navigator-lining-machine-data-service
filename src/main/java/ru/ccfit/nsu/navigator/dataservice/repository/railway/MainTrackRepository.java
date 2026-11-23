package ru.ccfit.nsu.navigator.dataservice.repository.railway;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.ccfit.nsu.navigator.dataservice.entity.railway.MainTrack;
import java.util.Optional;

public interface MainTrackRepository extends JpaRepository<MainTrack, Long> {

    Optional<MainTrack> findByDirection_IdAndSourceId(Long directionId, int sourceId);
}
