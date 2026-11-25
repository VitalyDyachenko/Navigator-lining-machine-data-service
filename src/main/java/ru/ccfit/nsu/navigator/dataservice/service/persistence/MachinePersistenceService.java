package ru.ccfit.nsu.navigator.dataservice.service.persistence;

import org.springframework.stereotype.Service;
import ru.ccfit.nsu.navigator.dataservice.entity.machine.Machine;
import ru.ccfit.nsu.navigator.dataservice.entity.machine.MachineParams;
import ru.ccfit.nsu.navigator.dataservice.exception.parsing.ParserException;
import ru.ccfit.nsu.navigator.dataservice.parser.common.model.MachineData;
import ru.ccfit.nsu.navigator.dataservice.repository.machine.MachineParamsRepository;
import ru.ccfit.nsu.navigator.dataservice.repository.machine.MachineRepository;

@Service
public class MachinePersistenceService {

	private final MachineRepository machineRepository;
	private final MachineParamsRepository machineParamsRepository;

	public MachinePersistenceService(
			MachineRepository machineRepository,
			MachineParamsRepository machineParamsRepository
	) {
		this.machineRepository = machineRepository;
		this.machineParamsRepository = machineParamsRepository;
	}

	public Machine getOrCreateMachine(MachineData source) {
		if (source.machineType() < 0 || source.machineType() > 4) {
			throw new ParserException("Unsupported machine type: " + source.machineType());
		}

		short typeId = (short) source.machineType();

		machineRepository.insertIfMissing(
				source.number(),
				source.name(),
				typeId
		);

		return machineRepository.findByNumberAndNameAndTypeId(
				source.number(),
				source.name(),
				typeId
		).orElseThrow(() -> new IllegalStateException(
				"Machine not found after insertIfMissing"
		));
	}

	public MachineParams getOrCreateMachineParams(Machine machine, MachineData source) {
		MachineParams params = new MachineParams(
				machine,
				source.continuousCorrection(),
				source.profileEnabled(),
				source.planeSatellite(),
				source.tampingSleepers(),
				source.alignmentArms(),
				source.levelingArms(),
				source.trolleyArms(),
				source.gaugeWidth()
		);

		return machineParamsRepository.findAllByMachine_Id(machine.getId())
				.stream()
				.filter(existing -> existing.hasSameSettings(params))
				.findFirst()
				.orElseGet(() -> machineParamsRepository.save(params));
	}
}