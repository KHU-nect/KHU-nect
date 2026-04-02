package com.khunect.backend.matching.dto.response;

import java.util.List;

public record FreePeriodMatchResponse(
	Long userId,
	String nickname,
	String major,
	List<InterestSummary> interests,
	String bio,
	String todayQuestion,
	List<FreeSlot> commonFreeSlots
) {

	public record InterestSummary(Long id, String name) {}

	public record FreeSlot(String day, String startTime, String endTime) {}
}
