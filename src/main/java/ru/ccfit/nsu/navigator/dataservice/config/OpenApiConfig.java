package ru.ccfit.nsu.navigator.dataservice.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
		info = @Info(
				title = "Navigator Lining Machine Data Service API",
				version = "1.0",
				description = "REST API для нашего сервиса"
		)
)
public class OpenApiConfig {

	// Глобально очищает схемы ответов (content) у всех эндпоинтов
	@Bean
	public OpenApiCustomizer hideAllResponseSchemas() {
		return openApi -> {
			if (openApi.getPaths() != null) {
				openApi.getPaths().values().forEach(pathItem ->
						pathItem.readOperations().forEach(operation -> {
							if (operation.getResponses() != null) {
								operation.getResponses().values().forEach(response ->
										response.setContent(null)
								);
							}
						})
				);
			}
		};
	}
}