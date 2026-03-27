package com.khunect.backend.match.controller;

import com.khunect.backend.common.response.ApiResponse;
import com.khunect.backend.match.dto.request.CreateMatchPostRequest;
import com.khunect.backend.match.dto.request.UpdateMatchPostRequest;
import com.khunect.backend.match.dto.response.AcceptMatchPostResponse;
import com.khunect.backend.match.dto.response.MatchPostListResponse;
import com.khunect.backend.match.dto.response.MatchPostResponse;
import com.khunect.backend.match.service.MatchPostService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/match-posts")
public class MatchPostController {

	private final MatchPostService matchPostService;

	public MatchPostController(MatchPostService matchPostService) {
		this.matchPostService = matchPostService;
	}

	@PostMapping
	public ApiResponse<MatchPostResponse> create(
		Authentication authentication,
		@Valid @RequestBody CreateMatchPostRequest request
	) {
		return ApiResponse.success(matchPostService.create(authentication.getName(), request));
	}

	@GetMapping
	public ApiResponse<MatchPostListResponse> getAll(
		@RequestParam(required = false) String status,
		@RequestParam(defaultValue = "0") @Min(0) int page,
		@RequestParam(defaultValue = "10") @Min(1) @Max(100) int size
	) {
		return ApiResponse.success(matchPostService.getAll(status, page, size));
	}

	@GetMapping("/{id}")
	public ApiResponse<MatchPostResponse> getById(@PathVariable Long id) {
		return ApiResponse.success(matchPostService.getById(id));
	}

	@PatchMapping("/{id}")
	public ApiResponse<MatchPostResponse> update(
		Authentication authentication,
		@PathVariable Long id,
		@Valid @RequestBody UpdateMatchPostRequest request
	) {
		return ApiResponse.success(matchPostService.update(authentication.getName(), id, request));
	}

	@DeleteMapping("/{id}")
	public ApiResponse<Void> delete(Authentication authentication, @PathVariable Long id) {
		matchPostService.delete(authentication.getName(), id);
		return ApiResponse.successWithoutData();
	}

	@PostMapping("/{id}/accept")
	public ApiResponse<AcceptMatchPostResponse> accept(Authentication authentication, @PathVariable Long id) {
		return ApiResponse.success(matchPostService.accept(authentication.getName(), id));
	}
}
