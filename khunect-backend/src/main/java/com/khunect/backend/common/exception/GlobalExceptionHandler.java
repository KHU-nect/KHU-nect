package com.khunect.backend.common.exception;

import com.khunect.backend.common.response.ApiResponse;
import com.khunect.backend.common.response.ErrorResponse;
import com.khunect.backend.common.response.ErrorResponse.FieldErrorDetail;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(CustomException.class)
	public ResponseEntity<ApiResponse<Void>> handleCustomException(CustomException exception) {
		ErrorCode errorCode = exception.getErrorCode();
		return buildResponse(errorCode, exception.getMessage(), null);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValid(MethodArgumentNotValidException exception) {
		return buildResponse(
			ErrorCode.INVALID_INPUT_VALUE,
			ErrorCode.INVALID_INPUT_VALUE.getMessage(),
			exception.getBindingResult().getFieldErrors().stream().map(this::toFieldErrorDetail).toList()
		);
	}

	@ExceptionHandler(BindException.class)
	public ResponseEntity<ApiResponse<Void>> handleBindException(BindException exception) {
		return buildResponse(
			ErrorCode.INVALID_INPUT_VALUE,
			ErrorCode.INVALID_INPUT_VALUE.getMessage(),
			exception.getBindingResult().getFieldErrors().stream().map(this::toFieldErrorDetail).toList()
		);
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException exception) {
		List<FieldErrorDetail> fieldErrors = List.of(
			new FieldErrorDetail(exception.getName(), exception.getValue(), exception.getMessage())
		);
		return buildResponse(ErrorCode.TYPE_MISMATCH, ErrorCode.TYPE_MISMATCH.getMessage(), fieldErrors);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ApiResponse<Void>> handleMessageNotReadable(HttpMessageNotReadableException exception) {
		return buildResponse(
			ErrorCode.HTTP_MESSAGE_NOT_READABLE,
			ErrorCode.HTTP_MESSAGE_NOT_READABLE.getMessage(),
			null
		);
	}

	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public ResponseEntity<ApiResponse<Void>> handleMethodNotAllowed(HttpRequestMethodNotSupportedException exception) {
		return buildResponse(ErrorCode.METHOD_NOT_ALLOWED, ErrorCode.METHOD_NOT_ALLOWED.getMessage(), null);
	}

	@ExceptionHandler(NoHandlerFoundException.class)
	public ResponseEntity<ApiResponse<Void>> handleNotFound(NoHandlerFoundException exception) {
		return buildResponse(ErrorCode.RESOURCE_NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND.getMessage(), null);
	}

	@ExceptionHandler(AuthenticationException.class)
	public ResponseEntity<ApiResponse<Void>> handleAuthentication(AuthenticationException exception) {
		return buildResponse(ErrorCode.UNAUTHORIZED, ErrorCode.UNAUTHORIZED.getMessage(), null);
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException exception) {
		return buildResponse(ErrorCode.FORBIDDEN, ErrorCode.FORBIDDEN.getMessage(), null);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<Void>> handleUnhandled(Exception exception) {
		return buildResponse(
			ErrorCode.INTERNAL_SERVER_ERROR,
			ErrorCode.INTERNAL_SERVER_ERROR.getMessage(),
			null
		);
	}

	private FieldErrorDetail toFieldErrorDetail(FieldError fieldError) {
		return new FieldErrorDetail(
			fieldError.getField(),
			fieldError.getRejectedValue(),
			fieldError.getDefaultMessage()
		);
	}

	private ResponseEntity<ApiResponse<Void>> buildResponse(
		ErrorCode errorCode,
		String message,
		List<FieldErrorDetail> fieldErrors
	) {
		ErrorResponse errorResponse = ErrorResponse.of(
			errorCode.getCode(),
			message,
			LocalDateTime.now(),
			fieldErrors
		);
		return ResponseEntity
			.status(errorCode.getStatus())
			.body(ApiResponse.error(errorResponse));
	}
}
