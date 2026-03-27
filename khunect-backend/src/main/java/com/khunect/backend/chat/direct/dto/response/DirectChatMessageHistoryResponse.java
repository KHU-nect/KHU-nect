package com.khunect.backend.chat.direct.dto.response;

import java.util.List;

public record DirectChatMessageHistoryResponse(
	List<DirectChatMessageResponse> content,
	Long nextBeforeMessageId,
	boolean hasNext
) {
}
