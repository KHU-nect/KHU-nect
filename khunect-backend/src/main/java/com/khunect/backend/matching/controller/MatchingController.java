package com.khunect.backend.matching.controller;

import com.khunect.backend.common.response.ApiResponse;
import com.khunect.backend.matching.dto.response.AcceptMatchResponse;
import com.khunect.backend.matching.dto.response.ClassMatchResponse;
import com.khunect.backend.matching.dto.response.FreePeriodMatchResponse;
import com.khunect.backend.matching.service.MatchingService;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/matching")
public class MatchingController {

	private final MatchingService matchingService;

	public MatchingController(MatchingService matchingService) {
		this.matchingService = matchingService;
	}

	/**
	 * 공강매칭: 공강 시간이 겹치는 사용자 목록
	 * GET /api/matching/free-period?interestId=1
	 */
	@GetMapping("/free-period")
	public ApiResponse<List<FreePeriodMatchResponse>> getFreePeriodMatches(
		Authentication authentication,
		@RequestParam(required = false) Long interestId
	) {
		return ApiResponse.success(matchingService.getFreePeriodMatches(authentication.getName(), interestId));
	}

	/**
	 * 수업매칭: 같은 수업을 듣는 사용자 목록
	 * GET /api/matching/class?interestId=1
	 */
	@GetMapping("/class")
	public ApiResponse<List<ClassMatchResponse>> getClassMatches(
		Authentication authentication,
		@RequestParam(required = false) Long interestId
	) {
		return ApiResponse.success(matchingService.getClassMatches(authentication.getName(), interestId));
	}

	/**
	 * 매칭 수락: 1:1 채팅방 생성 또는 기존 방 반환
	 * POST /api/matching/accept/{targetUserId}
	 */
	@PostMapping("/accept/{targetUserId}")
	public ApiResponse<AcceptMatchResponse> acceptMatch(
		Authentication authentication,
		@PathVariable Long targetUserId
	) {
		return ApiResponse.success(matchingService.acceptMatch(authentication.getName(), targetUserId));
	}
}
