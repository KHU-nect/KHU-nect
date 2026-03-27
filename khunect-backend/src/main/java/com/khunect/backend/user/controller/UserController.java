package com.khunect.backend.user.controller;

import com.khunect.backend.common.response.ApiResponse;
import com.khunect.backend.user.dto.request.CompleteSignupRequest;
import com.khunect.backend.user.dto.request.UpdateProfileRequest;
import com.khunect.backend.user.dto.response.MyProfileResponse;
import com.khunect.backend.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/me")
public class UserController {

	private final UserService userService;

	public UserController(UserService userService) {
		this.userService = userService;
	}

	@PostMapping("/signup-completion")
	public ApiResponse<MyProfileResponse> completeSignup(
		Authentication authentication,
		@Valid @RequestBody CompleteSignupRequest request
	) {
		return ApiResponse.success(userService.completeSignup(authentication.getName(), request));
	}

	@GetMapping
	public ApiResponse<MyProfileResponse> getMyProfile(Authentication authentication) {
		return ApiResponse.success(userService.getMyProfile(authentication.getName()));
	}

	@PatchMapping
	public ApiResponse<MyProfileResponse> updateProfile(
		Authentication authentication,
		@Valid @RequestBody UpdateProfileRequest request
	) {
		return ApiResponse.success(userService.updateProfile(authentication.getName(), request));
	}
}
