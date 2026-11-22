package ru.ccfit.nsu.navigator.dataservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.ccfit.nsu.navigator.dataservice.entity.Railway;
import java.util.Optional;

public interface RailwayRepository extends JpaRepository<Railway, Long> {

    Optional<Railway> findByCode(short code);
}