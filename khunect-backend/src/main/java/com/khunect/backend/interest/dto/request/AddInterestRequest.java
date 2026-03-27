package com.khunect.backend.interest.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddInterestRequest(
	@NotBlank(message = "관심사 이름은 필수입니다.")
	@Size(max = 30, message = "관심사 이름은 30자 이하여야 합니다.")
	String name
) {
}
