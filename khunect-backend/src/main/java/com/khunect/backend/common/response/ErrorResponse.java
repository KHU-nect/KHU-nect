package com.khunect.backend.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
	String code,
	String message,
	LocalDateTime timestamp,
	List<FieldErrorDetail> fieldErrors
) {

	public static ErrorResponse of(String code, String message, LocalDateTime timestamp) {
		return new ErrorResponse(code, message, timestamp, null);
	}

	public static ErrorResponse of(
		String code,
		String message,
		LocalDateTime timestamp,
		List<FieldErrorDetail> fieldErrors
	) {
		return new ErrorResponse(code, message, timestamp, fieldErrors);
	}

	public record FieldErrorDetail(
		String field,
		Object rejectedValue,
		String reason
	) {
	}
}
