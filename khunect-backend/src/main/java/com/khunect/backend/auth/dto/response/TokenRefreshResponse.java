package com.khunect.backend.auth.dto.response;

public record TokenRefreshResponse(
	String accessToken,
	String refreshToken
) {
}
