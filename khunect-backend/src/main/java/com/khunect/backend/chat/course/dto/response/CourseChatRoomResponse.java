package com.khunect.backend.chat.course.dto.response;

import java.time.LocalDateTime;

public record CourseChatRoomResponse(
	Long roomId,
	Long courseId,
	String courseName,
	String lastMessagePreview,
	LocalDateTime lastMessageTime,
	long unreadCount
) {
}
