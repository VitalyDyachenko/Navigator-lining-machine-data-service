package ru.ccfit.nsu.navigator.dataservice.repository.survey;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.ccfit.nsu.navigator.dataservice.entity.survey.GeoCoordinate;

public interface GeoCoordinateRepository extends JpaRepository<GeoCoordinate, Long> {
}