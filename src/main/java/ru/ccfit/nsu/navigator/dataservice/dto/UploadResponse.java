package ru.ccfit.nsu.navigator.dataservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Ответ на успешную загрузку файлов")
public record UploadResponse(
		@Schema(description = "ID созданной записи")
		Long id
) {}