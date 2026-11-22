package ru.ccfit.nsu.navigator.dataservice.repository.machine;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.ccfit.nsu.navigator.dataservice.entity.machine.Machine;

import java.util.Optional;

public interface MachineRepository extends JpaRepository<Machine, Long> {

    Optional<Machine> findFirstByNumberAndNameAndTypeId(
            Long number,
            String name,
            Short typeId
    );
}