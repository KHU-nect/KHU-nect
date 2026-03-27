package com.khunect.backend.course.dto.response;

import java.util.List;

public record CourseSearchResponse(
	List<CourseSummaryResponse> content,
	int page,
	int size,
	long totalElements,
	int totalPages,
	boolean hasNext
) {
}
