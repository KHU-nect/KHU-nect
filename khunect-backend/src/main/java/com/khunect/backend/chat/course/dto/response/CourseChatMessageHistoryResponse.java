package com.khunect.backend.chat.course.dto.response;

import java.util.List;

public record CourseChatMessageHistoryResponse(
	List<CourseChatMessageResponse> content,
	Long nextBeforeMessageId,
	boolean hasNext
) {
}
