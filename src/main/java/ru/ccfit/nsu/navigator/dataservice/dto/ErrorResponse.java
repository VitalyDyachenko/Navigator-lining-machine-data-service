package ru.ccfit.nsu.navigator.dataservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Ответ об ошибке")
public record ErrorResponse(
		@Schema(description = "Время ошибки", example = "2026-10-13T12:00:00Z")
		String timestamp,

		@Schema(description = "HTTP-код", example = "404")
		int status,

		@Schema(description = "Машинно-читаемый код ошибки", example = "WORK_NOT_FOUND")
		String code,

		@Schema(description = "Сообщение для человека", example = "Work with id 999 not found")
		String message,

		@Schema(description = "Путь запроса", example = "/api/works/999")
		String path,

		@Schema(description = "ID для связи с логами", example = "a3f8b2e1-4c5d-6e7f-8a9b-0c1d2e3f4a5b")
		String traceId
) {}