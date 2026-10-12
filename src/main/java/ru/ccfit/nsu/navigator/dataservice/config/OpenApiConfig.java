package ru.ccfit.nsu.navigator.dataservice.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

@OpenAPIDefinition(
		info = @Info(
				title = "Navigator Lining Machine Data Service API",
				version = "1.0",
				description = "REST API для нашего сервиса"
		)
)
public class OpenApiConfig {
}