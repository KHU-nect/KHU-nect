package com.khunect.backend.chat.course.dto.request;

import jakarta.validation.constraints.NotNull;

public record EnterCourseChatRoomRequest(
	@NotNull(message = "courseId는 필수입니다.")
	Long courseId,
	boolean createIfAbsent
) {
}
