package ru.ccfit.nsu.navigator.dataservice.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table (name = "original_files")
public class WorkFiles {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "survey_filename", nullable = false)
    private String surveyFilename;

    @JdbcTypeCode(SqlTypes.VARBINARY)
    @Column(name = "survey_file", nullable = false)
    private byte[] surveyFile;

    @Column(name = "calculation_filename", nullable = false)
    private String calculationFilename;

    @JdbcTypeCode(SqlTypes.VARBINARY)
    @Column(name = "calculation_file", nullable = false)
    private byte[] calculationFile;

    @Column(name = "work_filename", nullable = false)
    private String workFilename;

    @JdbcTypeCode(SqlTypes.VARBINARY)
    @Column(name = "work_file", nullable = false)
    private byte[] workFile;

    protected WorkFiles() {
    }

    public WorkFiles(
            String surveyFilename,
            byte[] surveyFile,
            String calculationFilename,
            byte[] calculationFile,
            String workFilename,
            byte[] workFile
    ) {
        this.surveyFilename = surveyFilename;
        this.surveyFile = surveyFile;
        this.calculationFilename = calculationFilename;
        this.calculationFile = calculationFile;
        this.workFilename = workFilename;
        this.workFile = workFile;
    }

    public Long getId() {
        return id;
    }

    public String getSurveyFilename() {
        return surveyFilename;
    }

    public byte[] getSurveyFile() {
        return surveyFile;
    }

    public String getCalculationFilename() {
        return calculationFilename;
    }

    public byte[] getCalculationFile() {
        return calculationFile;
    }

    public String getWorkFilename() {
        return workFilename;
    }

    public byte[] getWorkFile() {
        return workFile;
    }
}
