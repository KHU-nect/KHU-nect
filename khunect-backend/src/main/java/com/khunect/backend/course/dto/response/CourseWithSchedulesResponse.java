package com.khunect.backend.course.dto.response;

import java.util.List;

public record CourseWithSchedulesResponse(
	Long id,
	String courseCode,
	String courseName,
	String professorName,
	String departmentName,
	String college,
	boolean isOnline,
	String scheduleText,
	List<CourseScheduleSlotResponse> schedules
) {
}
