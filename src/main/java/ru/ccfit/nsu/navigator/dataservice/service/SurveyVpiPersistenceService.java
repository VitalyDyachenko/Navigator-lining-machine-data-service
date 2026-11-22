
package ru.ccfit.nsu.navigator.dataservice.service;

import org.springframework.stereotype.Service;
import ru.ccfit.nsu.navigator.dataservice.entity.*;
import ru.ccfit.nsu.navigator.dataservice.exception.SrvParsingException;
import ru.ccfit.nsu.navigator.dataservice.parser.srv.model.SrvVpiData;
import ru.ccfit.nsu.navigator.dataservice.parser.srv.model.track.SrvDefect;
import ru.ccfit.nsu.navigator.dataservice.parser.srv.model.vpiD.*;
import ru.ccfit.nsu.navigator.dataservice.repository.*;

import java.util.List;

@Service
public class SurveyVpiPersistenceService {

    private final SurveyFragmentRepository fragmentRepository;
    private final SurveyPostRepository postRepository;
    private final SurveySwitchRepository switchRepository;
    private final SurveyBuildRepository buildRepository;
    private final SurveySpeedRepository speedRepository;
    private final SurveyThreadRepository threadRepository;
    private final SurveyElevationRepository elevationRepository;
    private final SurveyCurveRepository curveRepository;
    private final SurveyDefectRepository defectRepository;


    public SurveyVpiPersistenceService(
            SurveyFragmentRepository fragmentRepository,
            SurveyPostRepository postRepository,
            SurveySwitchRepository switchRepository,
            SurveyBuildRepository buildRepository,
            SurveySpeedRepository speedRepository,
            SurveyThreadRepository threadRepository,
            SurveyElevationRepository elevationRepository,
            SurveyCurveRepository curveRepository,
            SurveyDefectRepository defectRepository)
    {
        this.fragmentRepository = fragmentRepository;
        this.postRepository = postRepository;
        this.switchRepository = switchRepository;
        this.buildRepository = buildRepository;
        this.speedRepository = speedRepository;
        this.threadRepository = threadRepository;
        this.elevationRepository = elevationRepository;
        this.curveRepository = curveRepository;
        this.defectRepository = defectRepository;
    }


    public void save(Long workId, SrvVpiData vpi) {
        saveFragment(workId, vpi.fragment());
        savePosts(workId, vpi.posts());
        saveSwitches(workId, vpi.switches());
        saveBuilds(workId, vpi.builds());
        saveSpeeds(workId, vpi.speeds());
        saveThreads(workId, vpi.threads());
        saveElevations(workId, vpi.elevations());
        saveCurves(workId, vpi.curves());
        saveDefects(workId, vpi.defects());
    }

    private void saveFragment(Long workId, SrvFragment source) {
        int direction = source.picketDirection();

        if (direction != 0 && direction != 1) {
            throw new SrvParsingException("Invalid fragment direction: " + direction);
        }

        SurveyFragment fragment = new SurveyFragment(
                workId,
                source.startStepIndex(),
                source.startKm(), source.startM(),
                source.endKm(), source.endM(),
                source.directionStationCode(),
                (short) source.parkCode(),
                source.trackNumber(),
                direction == 1,
                source.name()
        );

        fragmentRepository.save(fragment);
    }

    private void savePosts(Long workId, List<SrvPost> source) {
        List<SurveyPost> posts = source.stream()
                .map(post -> new SurveyPost(
                        workId,
                        post.stepIndex(),
                        post.kilometerNumber(),
                        post.fromPassport()
                ))
                .toList();

        postRepository.saveAll(posts);
    }

    private void saveSwitches(Long workId, List<SrvSwitch> source) {
        List<SurveySwitch> switches = source.stream()
                .map(item -> new SurveySwitch(
                        workId,
                        item.stepIndex(),
                        convertSwitchDirection(item.direction()),
                        (short) item.type(),
                        item.lengthM(),
                        item.number(),
                        item.stationCode(),
                        item.fromPassport()
                ))
                .toList();

        switchRepository.saveAll(switches);
    }

    private Boolean convertSwitchDirection(int direction) {
        return switch (direction) {
            case 0 -> null;
            case 1 -> true;
            case 2 -> false;
            default -> throw new SrvParsingException(
                    "Invalid switch direction: " + direction
            );
        };
    }

    private void saveBuilds(Long workId, List<SrvBuild> source) {
        List<SurveyBuild> builds = source.stream()
                .map(item -> new SurveyBuild(
                        workId,
                        item.startStepIndex(),
                        item.endStepIndex(),
                        item.type(),
                        item.fromPassport()
                ))
                .toList();

        buildRepository.saveAll(builds);
    }

    private void saveSpeeds(Long workId, List<SrvSpeed> source) {
        List<SurveySpeed> speeds = source.stream()
                .map(item -> new SurveySpeed(
                        workId,
                        item.startStepIndex(),
                        item.endStepIndex(),
                        item.startKm(),
                        (short) item.startPicket(),
                        item.endKm(),
                        (short) item.endPicket(),
                        item.passengerSpeed(),
                        item.freightSpeed(),
                        item.emptyTrainSpeed()
                ))
                .toList();

        speedRepository.saveAll(speeds);
    }

    private void saveThreads(Long workId, List<SrvThread> source) {
        List<SurveyThread> threads = source.stream()
                .map(item -> new SurveyThread(
                        workId,
                        item.startStepIndex(),
                        item.endStepIndex()
                ))
                .toList();

        threadRepository.saveAll(threads);
    }

    private void saveElevations(Long workId, List<SrvElevation> source) {
        List<SurveyElevation> elevations = source.stream()
                .map(item -> new SurveyElevation(
                        workId,
                        item.stepIndex(),
                        item.valueMm()
                ))
                .toList();

        elevationRepository.saveAll(elevations);
    }

    private void saveCurves(Long workId, List<SrvCurve> source) {
        List<SurveyCurve> curves = source.stream()
                .map(item -> new SurveyCurve(
                        workId,
                        item.stepIndex(),
                        item.radiusM(),
                        item.gaugeNormMm(),
                        item.wearMm(),
                        item.toleranceMm()
                ))
                .toList();

        curveRepository.saveAll(curves);
    }

    private void saveDefects(Long workId, List<SrvDefect> source) {
        List<SurveyDefect> defects = source.stream()
                .map(item -> new SurveyDefect(
                        workId,
                        item.startStepIndex(),
                        item.endStepIndex(),
                        item.mask(),
                        item.valueMm(),
                        item.typeCode(),
                        item.passengerSpeed(),
                        item.freightSpeed(),
                        item.emptyTrainSpeed(),
                        (short) item.degree(),
                        item.label()
                ))
                .toList();

        defectRepository.saveAll(defects);
    }

}
