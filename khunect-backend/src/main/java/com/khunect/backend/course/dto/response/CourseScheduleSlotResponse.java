package com.khunect.backend.course.dto.response;

public record CourseScheduleSlotResponse(
	String day,
	String startTime,
	String endTime,
	int startMinutes,
	int endMinutes,
	String classroom
) {
}
