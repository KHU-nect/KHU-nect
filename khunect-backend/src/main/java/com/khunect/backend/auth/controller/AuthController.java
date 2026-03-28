package com.khunect.backend.auth.controller;

import com.khunect.backend.auth.dto.request.AuthExchangeRequest;
import com.khunect.backend.auth.dto.request.LogoutRequest;
import com.khunect.backend.auth.dto.request.RefreshTokenRequest;
import com.khunect.backend.auth.dto.response.AuthExchangeResponse;
import com.khunect.backend.auth.dto.response.MeResponse;
import com.khunect.backend.auth.dto.response.TokenRefreshResponse;
import com.khunect.backend.auth.service.AuthService;
import com.khunect.backend.common.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	@PostMapping("/exchange")
	public ApiResponse<AuthExchangeResponse> exchange(@Valid @RequestBody AuthExchangeRequest request) {
		return ApiResponse.success(authService.exchange(request));
	}

	@PostMapping("/refresh")
	public ApiResponse<TokenRefreshResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
		return ApiResponse.success(authService.refresh(request));
	}

	@PostMapping("/logout")
	public ApiResponse<Void> logout(@Valid @RequestBody LogoutRequest request) {
		authService.logout(request);
		return ApiResponse.successWithoutData();
	}

	@GetMapping("/me")
	public ApiResponse<MeResponse> me(Authentication authentication) {
		return ApiResponse.success(authService.getMe(authentication.getName()));
	}
}
