package ru.ccfit.nsu.navigator.dataservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.ccfit.nsu.navigator.dataservice.entity.SurveyChannel;
import ru.ccfit.nsu.navigator.dataservice.entity.SurveyChannelId;

public interface SurveyChannelRepository
        extends JpaRepository<SurveyChannel, SurveyChannelId> {
}