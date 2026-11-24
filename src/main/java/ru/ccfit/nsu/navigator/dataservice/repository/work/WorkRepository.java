package ru.ccfit.nsu.navigator.dataservice.repository.work;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.ccfit.nsu.navigator.dataservice.entity.work.Work;

public interface WorkRepository extends JpaRepository<Work, Long> {
}
