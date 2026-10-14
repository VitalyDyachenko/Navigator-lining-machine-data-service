package ru.ccfit.nsu.navigator.dataservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Информация о файлах работы")
public record WorkResponse (
		@Schema(description = "Уникальный id работы")
		Long id,
		@Schema(description = "Имя загруженного файла съёмки")
		String surveyFilename,
		@Schema(description = "Имя загруженного файла проекта")
		String calculationFilename,
		@Schema(description = "Имя загруженного файла выправки")
		String workFilename
)
{}
