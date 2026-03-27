package com.khunect.backend.chat.direct.controller;

import com.khunect.backend.chat.direct.dto.response.DirectChatMessageHistoryResponse;
import com.khunect.backend.chat.direct.dto.response.DirectChatRoomResponse;
import com.khunect.backend.chat.direct.service.DirectChatService;
import com.khunect.backend.common.response.ApiResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/direct-chat/rooms")
public class DirectChatController {

	private final DirectChatService directChatService;

	public DirectChatController(DirectChatService directChatService) {
		this.directChatService = directChatService;
	}

	@GetMapping("/me")
	public ApiResponse<List<DirectChatRoomResponse>> getMyRooms(Authentication authentication) {
		return ApiResponse.success(directChatService.getMyRooms(authentication.getName()));
	}

	@GetMapping("/{roomId}/messages")
	public ApiResponse<DirectChatMessageHistoryResponse> getMessages(
		Authentication authentication,
		@PathVariable Long roomId,
		@RequestParam(required = false) Long beforeMessageId,
		@RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
	) {
		return ApiResponse.success(
			directChatService.getMessages(authentication.getName(), roomId, beforeMessageId, size)
		);
	}
}
