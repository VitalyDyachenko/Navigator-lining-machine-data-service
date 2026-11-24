
package ru.ccfit.nsu.navigator.dataservice.service;

import org.springframework.stereotype.Service;
import ru.ccfit.nsu.navigator.dataservice.entity.survey.SurveyChannel;
import ru.ccfit.nsu.navigator.dataservice.entity.survey.SurveyMarker;
import ru.ccfit.nsu.navigator.dataservice.entity.survey.SurveyRfid;
import ru.ccfit.nsu.navigator.dataservice.exception.parsing.SrvParsingException;
import ru.ccfit.nsu.navigator.dataservice.parser.srv.model.*;
import ru.ccfit.nsu.navigator.dataservice.parser.srv.model.channelInfo.SrvChannelInfo;
import ru.ccfit.nsu.navigator.dataservice.parser.srv.model.marker.SrvMarker;
import ru.ccfit.nsu.navigator.dataservice.parser.srv.model.rfid.SrvRfid;
import ru.ccfit.nsu.navigator.dataservice.repository.survey.SurveyChannelRepository;
import ru.ccfit.nsu.navigator.dataservice.repository.survey.SurveyMarkerRepository;
import ru.ccfit.nsu.navigator.dataservice.repository.survey.SurveyRfidRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class SurveyDataPersistenceService {

    private final SurveyChannelRepository channelRepository;
    private final SurveyMarkerRepository markerRepository;
    private final SurveyRfidRepository rfidRepository;

    public SurveyDataPersistenceService(SurveyChannelRepository channelRepository,
                                        SurveyMarkerRepository markerRepository,
                                        SurveyRfidRepository rfidRepository) {
        this.channelRepository = channelRepository;
        this.markerRepository = markerRepository;
        this.rfidRepository = rfidRepository;
    }

    public void save(Long workId, SrvSurvey source) {
        saveChannels(workId, source.header(), source.channelData());
        saveMarkers(workId, source.markers());
        saveRfids(workId, source.rfids());
    }

    private void saveChannels(Long workId, SrvHeader header, Map<Integer, byte[]> source) {
        SrvChannelInfo[] info = header.channels();
        List<SurveyChannel> channels = new ArrayList<>(source.size());

        for (Map.Entry<Integer, byte[]> entry : source.entrySet()) {
            int index = entry.getKey();

            if (index < 0 || index >= info.length || index > 15) {
                throw new SrvParsingException("Invalid channel index: " + index);
            }

            SrvChannelInfo metadata = info[index];

            channels.add(new SurveyChannel(
                    workId,
                    (short) index,
                    (short) metadata.valueType(),
                    (short) metadata.physicalType(),
                    metadata.scale(),
                    entry.getValue()
            ));
        }

        channelRepository.saveAll(channels);
    }

    private void saveMarkers(Long workId, List<SrvMarker> source) {
        List<SurveyMarker> markers = source.stream()
                .map(item -> new SurveyMarker(
                        workId,
                        (short) item.type(),
                        item.stepIndex()
                ))
                .toList();

        markerRepository.saveAll(markers);
    }

    private void saveRfids(Long workId, List<SrvRfid> source) {
        List<SurveyRfid> rfids = source.stream()
                .map(item -> new SurveyRfid(
                        workId,
                        item.syncPulseCount(),
                        item.tagIdentifier(),
                        item.dataBlocks(),
                        item.kilometer(),
                        (short) item.picket(),
                        (short) item.markerType(),
                        (short) item.blockMask(),
                        (short) item.pointCount(),
                        item.hasError(),
                        item.controlDistanceM()
                ))
                .toList();

        rfidRepository.saveAll(rfids);
    }
}
