package com.khunect.backend.mypage.controller;

import com.khunect.backend.common.response.ApiResponse;
import com.khunect.backend.mypage.dto.response.MyPageSummaryResponse;
import com.khunect.backend.mypage.service.MyPageService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mypage")
public class MyPageController {

	private final MyPageService myPageService;

	public MyPageController(MyPageService myPageService) {
		this.myPageService = myPageService;
	}

	@GetMapping("/summary")
	public ApiResponse<MyPageSummaryResponse> getSummary(Authentication authentication) {
		return ApiResponse.success(myPageService.getSummary(authentication.getName()));
	}
}
