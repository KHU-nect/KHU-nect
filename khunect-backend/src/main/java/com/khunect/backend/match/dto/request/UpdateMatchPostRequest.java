package com.khunect.backend.match.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateMatchPostRequest(
	@NotBlank(message = "선호 시간은 필수입니다.")
	@Size(max = 100, message = "선호 시간은 100자 이하여야 합니다.")
	String preferredTimeText,

	@NotBlank(message = "장소는 필수입니다.")
	@Size(max = 100, message = "장소는 100자 이하여야 합니다.")
	String locationText,

	@NotBlank(message = "내용은 필수입니다.")
	@Size(max = 1000, message = "내용은 1000자 이하여야 합니다.")
	String content,

	@Size(max = 30, message = "카테고리는 30자 이하여야 합니다.")
	String category
) {
}
