package com.khunect.backend.chat.course.dto.request;

import com.khunect.backend.chat.course.entity.CourseChatMessageMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SendCourseChatMessageRequest(
	@NotBlank(message = "메시지 내용은 필수입니다.")
	@Size(max = 1000, message = "메시지는 1000자 이하여야 합니다.")
	String content,
	CourseChatMessageMode mode
) {

	public CourseChatMessageMode modeOrDefault() {
		return mode == null ? CourseChatMessageMode.GENERAL : mode;
	}
}
