package ru.ccfit.nsu.navigator.dataservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record ErrorResponse(
		String timestamp,
		int status,
		String code,
		String message,
		String path,
		String traceId
) {}