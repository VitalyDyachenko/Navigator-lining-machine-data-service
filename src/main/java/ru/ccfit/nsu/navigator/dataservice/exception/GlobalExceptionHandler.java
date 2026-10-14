package ru.ccfit.nsu.navigator.dataservice.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import ru.ccfit.nsu.navigator.dataservice.dto.ErrorResponse;

import java.time.OffsetDateTime;

/**
 * Единая точка обработки ошибок внутри DispatcherServlet.
 * Гарантирует, что любая ошибка возвращается в формате {@link ErrorResponse}.
 *
 * Ошибки, которые возникают вне DispatcherServlet (в фильтрах, на уровне Tomcat),
 * перехватываются CustomErrorController (эндпоинт /error).
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
	private static final String TRACE_ID = "traceId";

	/**
	 * Бизнес-ошибки приложения.
	 */
	@ExceptionHandler(AppException.class)
	public ResponseEntity<ErrorResponse> handleAppException(
			AppException ex,
			HttpServletRequest request
	) {
		log.warn("AppException on {}: code={}, message={}",
				request.getRequestURI(), ex.getCode(), ex.getMessage());

		ErrorResponse error = buildError(
				ex.getStatus(),
				ex.getCode(),
				ex.getMessage(),
				request.getRequestURI()
		);
		return ResponseEntity.status(ex.getStatus()).body(error);
	}

	/**
	 * Ошибки валидации @Valid на @RequestBody.
	 */
	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(
			MethodArgumentNotValidException ex,
			HttpHeaders headers,
			HttpStatusCode status,
			WebRequest request
	) {
		String message = ex.getBindingResult().getFieldErrors().stream()
				.map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
				.reduce((a, b) -> a + "; " + b)
				.orElse("Validation failed");

		log.warn("Validation error on {}: {}", extractPath(request), message);

		ErrorResponse error = buildError(
				HttpStatus.BAD_REQUEST,
				"VALIDATION_ERROR",
				message,
				extractPath(request)
		);
		return new ResponseEntity<>(error, headers, HttpStatus.BAD_REQUEST);
	}

	/**
	 * Все стандартные Spring MVC исключения
	 * Родительский класс вызывает этот метод из своих @ExceptionHandler'ов.
	 * Мы сохраняем корректный HTTP-код, но приводим формат к нашему.
	 */
	@Override
	protected ResponseEntity<Object> handleExceptionInternal(
			Exception ex,
			Object body,
			HttpHeaders headers,
			HttpStatusCode statusCode,
			WebRequest request
	) {
		HttpStatus status = HttpStatus.valueOf(statusCode.value());

		if (status.is5xxServerError()) {
			log.error("Framework 5xx error on {}", extractPath(request), ex);
		} else {
			log.warn("Framework 4xx error on {}: {} — {}",
					extractPath(request), status.value(), ex.getMessage());
		}

		ErrorResponse error = buildError(
				status,
				status.name(),
				ex.getMessage(),
				extractPath(request)
		);
		return new ResponseEntity<>(error, headers, status);
	}

	/**
	 * Всё, что не попало выше (любые RuntimeException).
	 */
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleAnyException(
			Exception ex,
			HttpServletRequest request
	) {
		log.error("Unhandled exception on {}", request.getRequestURI(), ex);

		ErrorResponse error = buildError(
				HttpStatus.INTERNAL_SERVER_ERROR,
				"INTERNAL_ERROR",
				"Unexpected error",
				request.getRequestURI()
		);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
	}


	private ErrorResponse buildError(HttpStatus status, String code, String message, String path) {
		return new ErrorResponse(
				OffsetDateTime.now().toString(),
				status.value(),
				code,
				message,
				path,
				MDC.get(TRACE_ID)
		);
	}

	private String extractPath(WebRequest request) {
		if (request instanceof ServletWebRequest servletRequest) {
			return servletRequest.getRequest().getRequestURI();
		}
		return "";
	}
}