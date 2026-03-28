package com.khunect.backend.auth.dto.response;

public record AuthExchangeResponse(
	String accessToken,
	String refreshToken,
	boolean signupCompleted,
	AuthUserSummary user
) {
}
