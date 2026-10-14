package ru.ccfit.nsu.navigator.dataservice.exception;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.boot.webmvc.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.ccfit.nsu.navigator.dataservice.dto.ErrorResponse;

import java.time.OffsetDateTime;

@RestController
public class CustomErrorController implements ErrorController {

	@RequestMapping("/error")
	public ResponseEntity<ErrorResponse> handleError(HttpServletRequest request) {
		// Код ошибки, который установил Spring Boot
		Object statusAttr = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
		int statusCode = statusAttr != null ? Integer.parseInt(statusAttr.toString()) : 500;

		// от TraceIdFilter
		String traceId = MDC.get("traceId");

		ErrorResponse error = new ErrorResponse(
				OffsetDateTime.now().toString(),
				statusCode,
				HttpStatus.valueOf(statusCode).name(),
				HttpStatus.valueOf(statusCode).getReasonPhrase(),
				request.getRequestURI(),
				traceId
		);

		return ResponseEntity.status(statusCode).body(error);
	}
}
