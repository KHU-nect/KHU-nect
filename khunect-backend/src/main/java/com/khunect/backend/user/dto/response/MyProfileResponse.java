package com.khunect.backend.user.dto.response;

import java.util.List;

public record MyProfileResponse(
	Long userId,
	String email,
	String nickname,
	String major,
	String studentNumber,
	boolean signupCompleted,
	int point,
	int level,
	List<InterestSummary> interests,
	String bio,
	String todayQuestion
) {

	public record InterestSummary(
		Long id,
		String name
	) {
	}
}
