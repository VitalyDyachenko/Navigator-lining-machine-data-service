
package ru.ccfit.nsu.navigator.dataservice.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "survey_post")
public class SurveyPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "work_id", nullable = false)
    private Long workId;

    @Column(name = "step_index", nullable = false)
    private int stepIndex;

    @Column(name = "kilometer_number", nullable = false)
    private int kilometerNumber;

    @Column(name = "is_passport", nullable = false)
    private boolean fromPassport;

    protected SurveyPost() {
    }

    public SurveyPost(Long workId, int stepIndex, int kilometerNumber, boolean fromPassport) {
        this.workId = workId;
        this.stepIndex = stepIndex;
        this.kilometerNumber = kilometerNumber;
        this.fromPassport = fromPassport;
    }
}
