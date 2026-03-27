package com.khunect.backend.course.dto.response;

public record CourseSummaryResponse(
	Long id,
	String courseCode,
	String courseName,
	String professorName,
	String departmentName,
	String scheduleText,
	String classroom,
	int semesterYear,
	String semesterTerm,
	String sourceType
) {
}
