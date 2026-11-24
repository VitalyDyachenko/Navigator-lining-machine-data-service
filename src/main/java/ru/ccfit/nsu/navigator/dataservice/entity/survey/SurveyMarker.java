
package ru.ccfit.nsu.navigator.dataservice.entity.survey;

import jakarta.persistence.*;

@Entity
@Table(name = "survey_marker")
public class SurveyMarker {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "work_id", nullable = false)
    private Long workId;

    @Column(name = "type_id", nullable = false)
    private short typeId;

    @Column(name = "step_index", nullable = false)
    private long stepIndex;

    protected SurveyMarker() {
    }

    public SurveyMarker(Long workId, short typeId, long stepIndex) {
        this.workId = workId;
        this.typeId = typeId;
        this.stepIndex = stepIndex;
    }
}
