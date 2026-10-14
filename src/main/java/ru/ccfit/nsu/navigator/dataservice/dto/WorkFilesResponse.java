package ru.ccfit.nsu.navigator.dataservice.dto;

public record WorkFilesResponse(
		Long id,
		String surveyFilename,
		String calculationFilename,
		String workFilename
) {}