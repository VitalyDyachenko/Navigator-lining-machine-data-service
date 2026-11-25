package ru.ccfit.nsu.navigator.dataservice.service.persistence;

import org.springframework.stereotype.Service;
import ru.ccfit.nsu.navigator.dataservice.parser.common.model.GeoCoordinate;
import ru.ccfit.nsu.navigator.dataservice.repository.work.GeoCoordinateRepository;

import java.util.ArrayList;
import java.util.List;

@Service
public class GeoCoordinatePersistenceService {

	private final GeoCoordinateRepository geoCoordinateRepository;

	public GeoCoordinatePersistenceService(GeoCoordinateRepository geoCoordinateRepository) {
		this.geoCoordinateRepository = geoCoordinateRepository;
	}

	public void save(Long workId, boolean isProject, List<GeoCoordinate> source) {
		if (source == null || source.isEmpty()) {
			return;
		}

		List<ru.ccfit.nsu.navigator.dataservice.entity.work.GeoCoordinate> coordinates =
				new ArrayList<>(source.size());

		for (int i = 0; i < source.size(); i++) {
			GeoCoordinate item = source.get(i);

			coordinates.add(new ru.ccfit.nsu.navigator.dataservice.entity.work.GeoCoordinate(
					workId,
					isProject,
					i,
					item.latitude(),
					item.longitude(),
					item.altitude()
			));
		}

		geoCoordinateRepository.saveAll(coordinates);
	}
}