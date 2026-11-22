
package ru.ccfit.nsu.navigator.dataservice.service;

import org.springframework.stereotype.Service;
import ru.ccfit.nsu.navigator.dataservice.entity.*;
import ru.ccfit.nsu.navigator.dataservice.exception.SrvParsingException;
import ru.ccfit.nsu.navigator.dataservice.parser.srv.model.SrvVpiData;
import ru.ccfit.nsu.navigator.dataservice.parser.srv.model.vpiD.SrvFragment;
import ru.ccfit.nsu.navigator.dataservice.parser.srv.model.vpiD.SrvPost;
import ru.ccfit.nsu.navigator.dataservice.repository.*;

import java.util.List;

@Service
public class SurveyVpiPersistenceService {

    private final SurveyFragmentRepository fragmentRepository;
    private final SurveyPostRepository postRepository;

    public SurveyVpiPersistenceService(SurveyFragmentRepository fragmentRepository,
                                       SurveyPostRepository postRepository) {
        this.fragmentRepository = fragmentRepository;
        this.postRepository = postRepository;
    }

    public void save(Long workId, SrvVpiData vpi) {
        saveFragment(workId, vpi.fragment());
        savePosts(workId, vpi.posts());
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
}
