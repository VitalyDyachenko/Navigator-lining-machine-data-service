package ru.ccfit.nsu.navigator.dataservice.repository.survey;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.ccfit.nsu.navigator.dataservice.entity.survey.SurveyChannel;
import ru.ccfit.nsu.navigator.dataservice.entity.survey.SurveyChannelId;

public interface SurveyChannelRepository
        extends JpaRepository<SurveyChannel, SurveyChannelId> {
}