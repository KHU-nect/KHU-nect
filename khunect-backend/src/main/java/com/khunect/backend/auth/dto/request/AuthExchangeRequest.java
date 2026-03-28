package com.khunect.backend.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record AuthExchangeRequest(
	@NotBlank(message = "인증 코드는 필수입니다.")
	String code
) {
}
