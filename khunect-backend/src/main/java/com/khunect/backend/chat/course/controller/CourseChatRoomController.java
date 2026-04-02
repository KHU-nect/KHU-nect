package com.khunect.backend.chat.course.controller;

import com.khunect.backend.chat.course.dto.request.EnterCourseChatRoomRequest;
import com.khunect.backend.chat.course.dto.response.AcceptSitTogetherResponse;
import com.khunect.backend.chat.course.dto.response.CourseChatMessageHistoryResponse;
import com.khunect.backend.chat.course.dto.response.CourseChatRoomResponse;
import com.khunect.backend.chat.course.service.CourseChatService;
import com.khunect.backend.common.response.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/course-chat/rooms")
public class CourseChatRoomController {

	private final CourseChatService courseChatService;

	public CourseChatRoomController(CourseChatService courseChatService) {
		this.courseChatService = courseChatService;
	}

	@PostMapping("/enter")
	public ApiResponse<CourseChatRoomResponse> enterRoom(
		Authentication authentication,
		@Valid @RequestBody EnterCourseChatRoomRequest request
	) {
		return ApiResponse.success(courseChatService.enterRoom(authentication.getName(), request));
	}

	@GetMapping("/me")
	public ApiResponse<List<CourseChatRoomResponse>> getMyRooms(Authentication authentication) {
		return ApiResponse.success(courseChatService.getMyRooms(authentication.getName()));
	}

	@GetMapping("/{roomId}/messages")
	public ApiResponse<CourseChatMessageHistoryResponse> getMessages(
		Authentication authentication,
		@PathVariable Long roomId,
		@RequestParam(required = false) Long beforeMessageId,
		@RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
	) {
		return ApiResponse.success(
			courseChatService.getMessages(authentication.getName(), roomId, beforeMessageId, size)
		);
	}

	@PostMapping("/{roomId}/messages/{messageId}/sit-together/accept")
	public ApiResponse<AcceptSitTogetherResponse> acceptSitTogether(
		Authentication authentication,
		@PathVariable Long roomId,
		@PathVariable Long messageId
	) {
		return ApiResponse.success(
			courseChatService.acceptSitTogether(authentication.getName(), roomId, messageId)
		);
	}
}
