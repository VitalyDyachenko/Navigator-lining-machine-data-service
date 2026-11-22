package ru.ccfit.nsu.navigator.dataservice.repository.railway;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.ccfit.nsu.navigator.dataservice.entity.railway.Station;
import java.util.List;

public interface StationRepository extends JpaRepository<Station, Long> {

    List<Station> findAllBySourceId(int sourceId);
}