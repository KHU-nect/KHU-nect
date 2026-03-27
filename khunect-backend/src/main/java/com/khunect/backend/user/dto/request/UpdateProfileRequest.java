package com.khunect.backend.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
	@NotBlank(message = "닉네임은 필수입니다.")
	@Size(min = 2, max = 20, message = "닉네임은 2자 이상 20자 이하여야 합니다.")
	@Pattern(regexp = "^[A-Za-z0-9가-힣_]+$", message = "닉네임은 한글, 영문, 숫자, 언더스코어만 사용할 수 있습니다.")
	String nickname,

	@NotBlank(message = "전공은 필수입니다.")
	@Size(min = 2, max = 50, message = "전공은 2자 이상 50자 이하여야 합니다.")
	String major
) {
}
