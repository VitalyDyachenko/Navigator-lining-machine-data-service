
package ru.ccfit.nsu.navigator.dataservice.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "survey_channel")
@IdClass(SurveyChannelId.class)
public class SurveyChannel {

    @Id
    @Column(name = "work_id")
    private Long workId;

    @Id
    @Column(name = "channel_index")
    private Short channelIndex;

    @Column(name = "value_type", nullable = false)
    private short valueType;

    @Column(name = "physical_type_id", nullable = false)
    private short physicalTypeId;

    @Column(name = "scale", nullable = false)
    private float scale;

    @Column(name = "data", columnDefinition = "bytea")
    private byte[] data;

    protected SurveyChannel() {
    }

    public SurveyChannel(Long workId, short channelIndex, short valueType,
                         short physicalTypeId, float scale, byte[] data) {
        this.workId = workId;
        this.channelIndex = channelIndex;
        this.valueType = valueType;
        this.physicalTypeId = physicalTypeId;
        this.scale = scale;
        this.data = data.clone();
    }
}
