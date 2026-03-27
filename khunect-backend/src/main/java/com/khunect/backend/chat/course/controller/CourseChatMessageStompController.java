package com.khunect.backend.chat.course.controller;

import com.khunect.backend.chat.course.dto.request.SendCourseChatMessageRequest;
import com.khunect.backend.chat.course.dto.response.CourseChatMessageResponse;
import com.khunect.backend.chat.course.service.CourseChatService;
import jakarta.validation.Valid;
import java.security.Principal;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class CourseChatMessageStompController {

	private final CourseChatService courseChatService;
	private final SimpMessagingTemplate messagingTemplate;

	public CourseChatMessageStompController(
		CourseChatService courseChatService,
		SimpMessagingTemplate messagingTemplate
	) {
		this.courseChatService = courseChatService;
		this.messagingTemplate = messagingTemplate;
	}

	@MessageMapping("/course-chat/rooms/{roomId}/messages")
	public void sendMessage(
		@DestinationVariable Long roomId,
		@Valid SendCourseChatMessageRequest request,
		Principal principal
	) {
		CourseChatMessageResponse response = courseChatService.sendMessage(principal.getName(), roomId, request);
		messagingTemplate.convertAndSend("/sub/course-chat/rooms/" + roomId, response);
	}
}
