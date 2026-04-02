package com.khunect.backend.matching.dto.response;

import java.util.List;

public record ClassMatchResponse(
	Long userId,
	String nickname,
	String major,
	List<InterestSummary> interests,
	String bio,
	String todayQuestion,
	List<CommonCourse> commonCourses
) {

	public record InterestSummary(Long id, String name) {}

	public record CommonCourse(Long courseId, String courseName, String professorName) {}
}
