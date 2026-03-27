package com.khunect.backend.chat.direct.controller;

import com.khunect.backend.chat.direct.dto.request.SendDirectChatMessageRequest;
import com.khunect.backend.chat.direct.dto.response.DirectChatMessageResponse;
import com.khunect.backend.chat.direct.service.DirectChatService;
import jakarta.validation.Valid;
import java.security.Principal;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class DirectChatMessageStompController {

	private final DirectChatService directChatService;
	private final SimpMessagingTemplate messagingTemplate;

	public DirectChatMessageStompController(
		DirectChatService directChatService,
		SimpMessagingTemplate messagingTemplate
	) {
		this.directChatService = directChatService;
		this.messagingTemplate = messagingTemplate;
	}

	@MessageMapping("/direct-chat/rooms/{roomId}/messages")
	public void sendMessage(
		@DestinationVariable Long roomId,
		@Valid SendDirectChatMessageRequest request,
		Principal principal
	) {
		DirectChatMessageResponse response = directChatService.sendMessage(principal.getName(), roomId, request);
		messagingTemplate.convertAndSend("/sub/direct-chat/rooms/" + roomId, response);
	}
}
