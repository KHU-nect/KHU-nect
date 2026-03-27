package com.khunect.backend.chat.course.dto.response;

import java.time.LocalDateTime;

public record CourseChatMessageResponse(
	Long messageId,
	Long roomId,
	Long senderUserId,
	String senderNickname,
	String content,
	LocalDateTime createdAt
) {
}
