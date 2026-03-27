package com.khunect.backend.chat.direct.dto.response;

import java.time.LocalDateTime;

public record DirectChatRoomResponse(
	Long roomId,
	Long opponentUserId,
	String opponentNickname,
	String lastMessagePreview,
	LocalDateTime lastMessageTime
) {
}
