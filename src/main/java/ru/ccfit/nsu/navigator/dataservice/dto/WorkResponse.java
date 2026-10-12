package ru.ccfit.nsu.navigator.dataservice.dto;

public record WorkResponse (
    Long id,
    String surveyFilename,
    String calculateFilename,
    String workFilename
){

}
