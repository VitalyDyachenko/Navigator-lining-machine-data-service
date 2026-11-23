package ru.ccfit.nsu.navigator.dataservice.repository.machine;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.ccfit.nsu.navigator.dataservice.entity.machine.MachineParams;

import java.util.List;

public interface MachineParamsRepository
        extends JpaRepository<MachineParams, Long> {

    List<MachineParams> findAllByMachine_Id(Long machineId);
}