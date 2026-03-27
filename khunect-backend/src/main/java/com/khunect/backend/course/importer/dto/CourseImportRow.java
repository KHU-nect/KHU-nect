package com.khunect.backend.course.importer.dto;

public record CourseImportRow(
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
