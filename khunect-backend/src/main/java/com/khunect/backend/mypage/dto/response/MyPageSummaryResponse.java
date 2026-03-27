package com.khunect.backend.mypage.dto.response;

public record MyPageSummaryResponse(
	String nickname,
	String major,
	String maskedStudentNumber,
	int point,
	int level,
	long registeredCourseCount,
	int successfulMatchCount,
	int helpedCount,
	WeeklyStats weeklyStats
) {

	public record WeeklyStats(
		long courseChatMessageCount,
		long directChatMessageCount,
		long matchPostCreatedCount,
		long acceptedMatchCount
	) {
	}
}
