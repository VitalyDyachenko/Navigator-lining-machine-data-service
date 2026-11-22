package ru.ccfit.nsu.navigator.dataservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.ccfit.nsu.navigator.dataservice.entity.GeoCoordinate;

public interface GeoCoordinateRepository extends JpaRepository<GeoCoordinate, Long> {
}