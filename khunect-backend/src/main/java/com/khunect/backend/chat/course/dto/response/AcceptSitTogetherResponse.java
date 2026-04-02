package com.khunect.backend.chat.course.dto.response;

public record AcceptSitTogetherResponse(
	Long sourceMessageId,
	Long sourceRoomId,
	Long directRoomId
) {
}
