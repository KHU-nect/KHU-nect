package com.khunect.backend.interest.controller;

import com.khunect.backend.common.response.ApiResponse;
import com.khunect.backend.interest.dto.request.AddInterestRequest;
import com.khunect.backend.interest.dto.response.MyInterestResponse;
import com.khunect.backend.interest.service.InterestService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/interests/me")
public class InterestController {

	private final InterestService interestService;

	public InterestController(InterestService interestService) {
		this.interestService = interestService;
	}

	@GetMapping
	public ApiResponse<List<MyInterestResponse>> getMyInterests(Authentication authentication) {
		return ApiResponse.success(interestService.getMyInterests(authentication.getName()));
	}

	@PostMapping
	public ApiResponse<MyInterestResponse> addInterest(
		Authentication authentication,
		@Valid @RequestBody AddInterestRequest request
	) {
		return ApiResponse.success(interestService.addInterest(authentication.getName(), request));
	}

	@DeleteMapping("/{interestId}")
	public ApiResponse<Void> removeInterest(
		Authentication authentication,
		@PathVariable Long interestId
	) {
		interestService.removeInterest(authentication.getName(), interestId);
		return ApiResponse.successWithoutData();
	}
}
