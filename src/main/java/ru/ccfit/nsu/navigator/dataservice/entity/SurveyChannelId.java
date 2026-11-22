package ru.ccfit.nsu.navigator.dataservice.entity;

import java.io.Serializable;
import java.util.Objects;

public class SurveyChannelId implements Serializable {

    private Long workId;
    private Short channelIndex;

    public SurveyChannelId() {
    }

    public SurveyChannelId(Long workId, Short channelIndex) {
        this.workId = workId;
        this.channelIndex = channelIndex;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof SurveyChannelId other)) return false;

        return Objects.equals(workId, other.workId)
                && Objects.equals(channelIndex, other.channelIndex);
    }

    @Override
    public int hashCode() {
        return Objects.hash(workId, channelIndex);
    }
}
