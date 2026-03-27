package com.khunect.backend.chat.direct.dto.response;

import java.time.LocalDateTime;

public record DirectChatMessageResponse(
	Long messageId,
	Long roomId,
	Long senderUserId,
	String senderNickname,
	String content,
	LocalDateTime createdAt
) {
}
