package com.khunect.backend.auth.dto.response;

public record MeResponse(
	Long id,
	String email,
	String nickname,
	String major,
	String studentNumber,
	boolean signupCompleted
) {
}
