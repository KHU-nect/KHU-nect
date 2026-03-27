package com.khunect.backend.timetable.dto.request;

import jakarta.validation.constraints.NotNull;

public record AddTimetableEntryRequest(
	@NotNull(message = "courseId는 필수입니다.")
	Long courseId
) {
}
