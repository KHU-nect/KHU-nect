package com.khunect.backend.chat.direct.service;

import com.khunect.backend.chat.direct.dto.request.SendDirectChatMessageRequest;
import com.khunect.backend.chat.direct.dto.response.DirectChatMessageHistoryResponse;
import com.khunect.backend.chat.direct.dto.response.DirectChatMessageResponse;
import com.khunect.backend.chat.direct.dto.response.DirectChatRoomResponse;
import com.khunect.backend.chat.direct.entity.DirectChatMessage;
import com.khunect.backend.chat.direct.entity.DirectChatRoom;
import com.khunect.backend.chat.direct.repository.DirectChatMessageRepository;
import com.khunect.backend.chat.direct.repository.DirectChatRoomRepository;
import com.khunect.backend.common.exception.CustomException;
import com.khunect.backend.common.exception.ErrorCode;
import com.khunect.backend.user.entity.User;
import com.khunect.backend.user.service.UserService;
import java.util.List;
import java.util.Optional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DirectChatService {

	private final DirectChatRoomRepository directChatRoomRepository;
	private final DirectChatMessageRepository directChatMessageRepository;
	private final UserService userService;

	public DirectChatService(
		DirectChatRoomRepository directChatRoomRepository,
		DirectChatMessageRepository directChatMessageRepository,
		UserService userService
	) {
		this.directChatRoomRepository = directChatRoomRepository;
		this.directChatMessageRepository = directChatMessageRepository;
		this.userService = userService;
	}

	@Transactional
	public DirectChatRoom getOrCreateRoom(User firstUser, User secondUser) {
		long firstId = Math.min(firstUser.getId(), secondUser.getId());
		long secondId = Math.max(firstUser.getId(), secondUser.getId());
		return directChatRoomRepository.findByParticipantOneIdAndParticipantTwoId(firstId, secondId)
			.orElseGet(() -> {
				try {
					return directChatRoomRepository.save(DirectChatRoom.create(firstUser, secondUser));
				} catch (DataIntegrityViolationException exception) {
					return directChatRoomRepository.findByParticipantOneIdAndParticipantTwoId(firstId, secondId)
						.orElseThrow(() -> exception);
				}
			});
	}

	public List<DirectChatRoomResponse> getMyRooms(String email) {
		User user = userService.getOrCreateUser(email);
		return directChatRoomRepository.findAllByUserId(user.getId()).stream()
			.map(room -> toRoomResponse(room, user.getId()))
			.toList();
	}

	public DirectChatMessageHistoryResponse getMessages(String email, Long roomId, Long beforeMessageId, int size) {
		User user = userService.getOrCreateUser(email);
		validateParticipant(roomId, user.getId());

		int fetchSize = size + 1;
		List<DirectChatMessage> messages = beforeMessageId == null
			? directChatMessageRepository.findLatestByRoomId(roomId, PageRequest.of(0, fetchSize))
			: directChatMessageRepository.findHistoryByRoomIdAndBeforeMessageId(roomId, beforeMessageId, PageRequest.of(0, fetchSize));

		boolean hasNext = messages.size() > size;
		List<DirectChatMessageResponse> content = messages.stream()
			.limit(size)
			.map(this::toMessageResponse)
			.toList();

		Long nextBeforeMessageId = hasNext && !content.isEmpty()
			? content.get(content.size() - 1).messageId()
			: null;

		return new DirectChatMessageHistoryResponse(content, nextBeforeMessageId, hasNext);
	}

	@Transactional
	public DirectChatMessageResponse sendMessage(String email, Long roomId, SendDirectChatMessageRequest request) {
		User user = userService.getOrCreateUser(email);
		DirectChatRoom room = validateParticipant(roomId, user.getId());
		String normalizedContent = request.content().trim().replaceAll("\\s{2,}", " ");

		DirectChatMessage message = directChatMessageRepository.save(DirectChatMessage.create(room, user, normalizedContent));
		room.updateLastMessageTime(message.getCreatedAt());
		return toMessageResponse(message);
	}

	public boolean isParticipant(String email, Long roomId) {
		User user = userService.getOrCreateUser(email);
		DirectChatRoom room = directChatRoomRepository.findById(roomId).orElse(null);
		return room != null && isParticipant(room, user.getId());
	}

	private DirectChatRoom validateParticipant(Long roomId, Long userId) {
		DirectChatRoom room = directChatRoomRepository.findById(roomId)
			.orElseThrow(() -> new CustomException(ErrorCode.DIRECT_CHAT_ROOM_NOT_FOUND));
		if (!isParticipant(room, userId)) {
			throw new CustomException(ErrorCode.DIRECT_CHAT_ROOM_MEMBERSHIP_REQUIRED);
		}
		return room;
	}

	private boolean isParticipant(DirectChatRoom room, Long userId) {
		return room.getParticipantOne().getId().equals(userId) || room.getParticipantTwo().getId().equals(userId);
	}

	private DirectChatRoomResponse toRoomResponse(DirectChatRoom room, Long myUserId) {
		User opponent = room.getParticipantOne().getId().equals(myUserId)
			? room.getParticipantTwo()
			: room.getParticipantOne();
		Optional<DirectChatMessage> latestMessage = directChatMessageRepository.findFirstByRoomIdOrderByIdDesc(room.getId());
		return new DirectChatRoomResponse(
			room.getId(),
			opponent.getId(),
			opponent.getNickname(),
			latestMessage.map(DirectChatMessage::getContent).orElse(null),
			room.getLastMessageTime()
		);
	}

	private DirectChatMessageResponse toMessageResponse(DirectChatMessage message) {
		return new DirectChatMessageResponse(
			message.getId(),
			message.getRoom().getId(),
			message.getSender().getId(),
			message.getSender().getNickname(),
			message.getContent(),
			message.getCreatedAt()
		);
	}
}
