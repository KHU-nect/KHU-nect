package com.khunect.backend.chat.course.service;

import com.khunect.backend.chat.course.dto.request.EnterCourseChatRoomRequest;
import com.khunect.backend.chat.course.dto.request.SendCourseChatMessageRequest;
import com.khunect.backend.chat.course.dto.response.CourseChatMessageHistoryResponse;
import com.khunect.backend.chat.course.dto.response.CourseChatMessageResponse;
import com.khunect.backend.chat.course.dto.response.CourseChatRoomResponse;
import com.khunect.backend.chat.course.entity.CourseChatMessage;
import com.khunect.backend.chat.course.entity.CourseChatRoom;
import com.khunect.backend.chat.course.entity.CourseChatRoomMembership;
import com.khunect.backend.chat.course.repository.CourseChatMessageRepository;
import com.khunect.backend.chat.course.repository.CourseChatRoomMembershipRepository;
import com.khunect.backend.chat.course.repository.CourseChatRoomRepository;
import com.khunect.backend.common.exception.CustomException;
import com.khunect.backend.common.exception.ErrorCode;
import com.khunect.backend.course.entity.Course;
import com.khunect.backend.course.service.CourseService;
import com.khunect.backend.timetable.repository.TimetableEntryRepository;
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
public class CourseChatService {

	private final CourseChatRoomRepository courseChatRoomRepository;
	private final CourseChatRoomMembershipRepository membershipRepository;
	private final CourseChatMessageRepository messageRepository;
	private final CourseService courseService;
	private final UserService userService;
	private final TimetableEntryRepository timetableEntryRepository;

	public CourseChatService(
		CourseChatRoomRepository courseChatRoomRepository,
		CourseChatRoomMembershipRepository membershipRepository,
		CourseChatMessageRepository messageRepository,
		CourseService courseService,
		UserService userService,
		TimetableEntryRepository timetableEntryRepository
	) {
		this.courseChatRoomRepository = courseChatRoomRepository;
		this.membershipRepository = membershipRepository;
		this.messageRepository = messageRepository;
		this.courseService = courseService;
		this.userService = userService;
		this.timetableEntryRepository = timetableEntryRepository;
	}

	@Transactional
	public CourseChatRoomResponse enterRoom(String email, EnterCourseChatRoomRequest request) {
		User user = userService.getOrCreateUser(email);
		if (!timetableEntryRepository.existsByUserIdAndCourseId(user.getId(), request.courseId())) {
			throw new CustomException(ErrorCode.COURSE_CHAT_ROOM_NOT_JOINABLE);
		}

		CourseChatRoom room = courseChatRoomRepository.findByCourseId(request.courseId())
			.orElseGet(() -> {
				if (!request.createIfAbsent()) {
					throw new CustomException(ErrorCode.COURSE_CHAT_ROOM_CREATION_REQUIRED);
				}
				Course course = courseService.findCourse(request.courseId());
				try {
					return courseChatRoomRepository.save(CourseChatRoom.create(course));
				} catch (DataIntegrityViolationException exception) {
					return courseChatRoomRepository.findByCourseId(request.courseId())
						.orElseThrow(() -> exception);
				}
			});

		membershipRepository.findByRoomIdAndUserId(room.getId(), user.getId())
			.orElseGet(() -> membershipRepository.save(CourseChatRoomMembership.create(room, user)));

		return toRoomResponse(room);
	}

	public List<CourseChatRoomResponse> getMyRooms(String email) {
		User user = userService.getOrCreateUser(email);
		return membershipRepository.findAllByUserIdWithRoomAndCourse(user.getId()).stream()
			.map(CourseChatRoomMembership::getRoom)
			.map(this::toRoomResponse)
			.toList();
	}

	public CourseChatMessageHistoryResponse getMessages(String email, Long roomId, Long beforeMessageId, int size) {
		User user = userService.getOrCreateUser(email);
		validateMembership(roomId, user.getId());

		int fetchSize = size + 1;
		List<CourseChatMessage> messages = beforeMessageId == null
			? messageRepository.findLatestByRoomId(roomId, PageRequest.of(0, fetchSize))
			: messageRepository.findHistoryByRoomIdAndBeforeMessageId(roomId, beforeMessageId, PageRequest.of(0, fetchSize));

		boolean hasNext = messages.size() > size;
		List<CourseChatMessageResponse> content = messages.stream()
			.limit(size)
			.map(this::toMessageResponse)
			.toList();

		Long nextBeforeMessageId = hasNext && !content.isEmpty()
			? content.get(content.size() - 1).messageId()
			: null;

		return new CourseChatMessageHistoryResponse(content, nextBeforeMessageId, hasNext);
	}

	@Transactional
	public CourseChatMessageResponse sendMessage(String email, Long roomId, SendCourseChatMessageRequest request) {
		User user = userService.getOrCreateUser(email);
		CourseChatRoom room = validateMembership(roomId, user.getId());
		String normalizedContent = request.content().trim().replaceAll("\\s{2,}", " ");

		CourseChatMessage message = messageRepository.save(CourseChatMessage.create(room, user, normalizedContent));
		room.updateLastMessageTime(message.getCreatedAt());
		return toMessageResponse(message);
	}

	public boolean isMember(String email, Long roomId) {
		User user = userService.getOrCreateUser(email);
		return membershipRepository.existsByRoomIdAndUserId(roomId, user.getId());
	}

	private CourseChatRoom validateMembership(Long roomId, Long userId) {
		CourseChatRoom room = courseChatRoomRepository.findById(roomId)
			.orElseThrow(() -> new CustomException(ErrorCode.COURSE_CHAT_ROOM_NOT_FOUND));
		if (!membershipRepository.existsByRoomIdAndUserId(roomId, userId)) {
			throw new CustomException(ErrorCode.COURSE_CHAT_ROOM_MEMBERSHIP_REQUIRED);
		}
		return room;
	}

	private CourseChatRoomResponse toRoomResponse(CourseChatRoom room) {
		Optional<CourseChatMessage> latestMessage = messageRepository.findFirstByRoomIdOrderByIdDesc(room.getId());
		return new CourseChatRoomResponse(
			room.getId(),
			room.getCourse().getId(),
			room.getCourse().getCourseName(),
			latestMessage.map(CourseChatMessage::getContent).orElse(null),
			room.getLastMessageTime(),
			0L
		);
	}

	private CourseChatMessageResponse toMessageResponse(CourseChatMessage message) {
		return new CourseChatMessageResponse(
			message.getId(),
			message.getRoom().getId(),
			message.getSender().getId(),
			message.getSender().getNickname(),
			message.getContent(),
			message.getCreatedAt()
		);
	}
}
