package com.khunect.backend.match.dto.response;

import java.util.List;

public record MatchPostListResponse(
	List<MatchPostResponse> content,
	int page,
	int size,
	long totalElements,
	int totalPages,
	boolean hasNext
) {
}
