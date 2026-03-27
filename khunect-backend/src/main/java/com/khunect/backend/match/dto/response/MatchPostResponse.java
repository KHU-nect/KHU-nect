package com.khunect.backend.match.dto.response;

import java.time.LocalDateTime;

public record MatchPostResponse(
	Long id,
	Long authorUserId,
	String authorNickname,
	String preferredTimeText,
	String locationText,
	String content,
	String category,
	String status,
	Long acceptedByUserId,
	LocalDateTime acceptedAt,
	LocalDateTime createdAt,
	LocalDateTime updatedAt
) {
}
