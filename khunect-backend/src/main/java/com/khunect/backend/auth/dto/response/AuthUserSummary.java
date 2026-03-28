package com.khunect.backend.auth.dto.response;

public record AuthUserSummary(
	Long id,
	String email,
	String nickname,
	String major,
	String studentNumber
) {
}
