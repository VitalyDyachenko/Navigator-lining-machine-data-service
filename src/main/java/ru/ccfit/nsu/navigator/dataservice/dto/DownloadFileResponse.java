package ru.ccfit.nsu.navigator.dataservice.dto;

public record DownloadFileResponse(
		String filename,
		byte[] content
) {}
