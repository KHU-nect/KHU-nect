package com.khunect.backend.chat.course.config;

import com.khunect.backend.chat.course.service.CourseChatService;
import com.khunect.backend.chat.direct.service.DirectChatService;
import com.khunect.backend.common.exception.CustomException;
import com.khunect.backend.common.exception.ErrorCode;
import com.khunect.backend.common.security.JwtTokenProvider;
import java.security.Principal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class CourseChatStompChannelInterceptor implements ChannelInterceptor {

	private static final Pattern COURSE_ROOM_DESTINATION_PATTERN =
		Pattern.compile("^/(pub|sub)/course-chat/rooms/(\\d+)(/messages)?$");
	private static final Pattern DIRECT_ROOM_DESTINATION_PATTERN =
		Pattern.compile("^/(pub|sub)/direct-chat/rooms/(\\d+)(/messages)?$");

	private final JwtTokenProvider jwtTokenProvider;
	private final CourseChatService courseChatService;
	private final DirectChatService directChatService;

	public CourseChatStompChannelInterceptor(
		JwtTokenProvider jwtTokenProvider,
		CourseChatService courseChatService,
		DirectChatService directChatService
	) {
		this.jwtTokenProvider = jwtTokenProvider;
		this.courseChatService = courseChatService;
		this.directChatService = directChatService;
	}

	@Override
	public Message<?> preSend(Message<?> message, MessageChannel channel) {
		StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
		if (accessor == null || accessor.getCommand() == null) {
			return message;
		}

		if (StompCommand.CONNECT.equals(accessor.getCommand())) {
			String authorizationHeader = accessor.getFirstNativeHeader("Authorization");
			if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
				throw new CustomException(ErrorCode.UNAUTHORIZED);
			}
			Principal principal = (Principal) jwtTokenProvider.getAuthentication(authorizationHeader.substring(7));
			accessor.setUser(principal);
			return message;
		}

		if (StompCommand.SUBSCRIBE.equals(accessor.getCommand()) || StompCommand.SEND.equals(accessor.getCommand())) {
			Principal principal = accessor.getUser();
			if (principal == null) {
				throw new CustomException(ErrorCode.UNAUTHORIZED);
			}
			Long courseRoomId = extractRoomId(accessor.getDestination(), COURSE_ROOM_DESTINATION_PATTERN);
			if (courseRoomId != null && !courseChatService.isMember(principal.getName(), courseRoomId)) {
				throw new CustomException(ErrorCode.COURSE_CHAT_ROOM_MEMBERSHIP_REQUIRED);
			}
			Long directRoomId = extractRoomId(accessor.getDestination(), DIRECT_ROOM_DESTINATION_PATTERN);
			if (directRoomId != null && !directChatService.isParticipant(principal.getName(), directRoomId)) {
				throw new CustomException(ErrorCode.DIRECT_CHAT_ROOM_MEMBERSHIP_REQUIRED);
			}
		}

		return message;
	}

	private Long extractRoomId(String destination, Pattern pattern) {
		if (destination == null) {
			return null;
		}
		Matcher matcher = pattern.matcher(destination);
		if (!matcher.matches()) {
			return null;
		}
		return Long.parseLong(matcher.group(2));
	}
}
