package com.khunect.backend.match.dto.response;

public record AcceptMatchPostResponse(
	Long matchPostId,
	String status,
	Long directRoomId
) {
}
