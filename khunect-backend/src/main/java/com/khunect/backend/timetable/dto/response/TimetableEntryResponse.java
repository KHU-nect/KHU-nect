package com.khunect.backend.timetable.dto.response;

public record TimetableEntryResponse(
	Long entryId,
	Long courseId,
	String courseCode,
	String courseName,
	String professorName,
	String departmentName,
	String scheduleText,
	String classroom,
	int semesterYear,
	String semesterTerm
) {
}
