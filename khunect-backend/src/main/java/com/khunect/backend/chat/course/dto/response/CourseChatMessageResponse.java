package com.khunect.backend.chat.course.dto.response;

import com.khunect.backend.chat.course.entity.CourseChatMessageMode;
import com.khunect.backend.chat.course.entity.SitTogetherRequestStatus;
import java.time.LocalDateTime;

public record CourseChatMessageResponse(
	Long messageId,
	Long roomId,
	Long senderUserId,
	String senderNickname,
	String content,
	CourseChatMessageMode mode,
	SitTogetherRequestStatus sitTogetherStatus,
	Long sitTogetherDirectRoomId,
	LocalDateTime createdAt
) {
}
