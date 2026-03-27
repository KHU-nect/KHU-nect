package com.khunect.backend.chat.course;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.khunect.backend.chat.course.dto.response.CourseChatMessageResponse;
import com.khunect.backend.chat.course.entity.CourseChatMessage;
import com.khunect.backend.chat.course.entity.CourseChatRoom;
import com.khunect.backend.chat.course.repository.CourseChatMessageRepository;
import com.khunect.backend.chat.course.repository.CourseChatRoomMembershipRepository;
import com.khunect.backend.chat.course.repository.CourseChatRoomRepository;
import com.khunect.backend.common.security.JwtTokenProvider;
import com.khunect.backend.course.entity.Course;
import com.khunect.backend.course.entity.CourseSourceType;
import com.khunect.backend.course.entity.SemesterTerm;
import com.khunect.backend.course.repository.CourseRepository;
import com.khunect.backend.timetable.entity.TimetableEntry;
import com.khunect.backend.timetable.repository.TimetableEntryRepository;
import com.khunect.backend.user.entity.User;
import com.khunect.backend.user.repository.UserRepository;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CourseChatIntegrationTest {

	private static final String USER_EMAIL = "chat-user@khu.ac.kr";
	private static final String OTHER_USER_EMAIL = "chat-user-2@khu.ac.kr";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private CourseRepository courseRepository;

	@Autowired
	private TimetableEntryRepository timetableEntryRepository;

	@Autowired
	private CourseChatRoomRepository roomRepository;

	@Autowired
	private CourseChatRoomMembershipRepository membershipRepository;

	@Autowired
	private CourseChatMessageRepository messageRepository;

	@Autowired
	private JwtTokenProvider jwtTokenProvider;

	@LocalServerPort
	private int port;

	private WebSocketStompClient stompClient;
	private Course course;

	@BeforeEach
	void setUp() {
		messageRepository.deleteAll();
		membershipRepository.deleteAll();
		roomRepository.deleteAll();
		timetableEntryRepository.deleteAll();
		courseRepository.deleteAll();
		userRepository.deleteAll();

		stompClient = new WebSocketStompClient(new StandardWebSocketClient());
		stompClient.setMessageConverter(new MappingJackson2MessageConverter());

		course = courseRepository.save(Course.builder()
			.courseCode("CSE310")
			.courseName("웹서비스백엔드")
			.professorName("한지원")
			.departmentName("컴퓨터공학과")
			.scheduleText("화 15:00-16:30, 목 15:00-16:30")
			.classroom("전자정보대 412")
			.semesterYear(2026)
			.semesterTerm(SemesterTerm.FIRST)
			.sourceType(CourseSourceType.MANUAL)
			.build());
	}

	@AfterEach
	void tearDown() {
		if (stompClient != null) {
			stompClient.stop();
		}
	}

	@Test
	void enterFailsWhenUserDoesNotHaveCourseInTimetable() throws Exception {
		userRepository.save(completedUser(USER_EMAIL, "2024123456"));

		mockMvc.perform(post("/api/course-chat/rooms/enter")
				.with(user(USER_EMAIL))
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "courseId": %d,
					  "createIfAbsent": true
					}
					""".formatted(course.getId())))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.error.code").value("COURSE-CHAT-403-1"));
	}

	@Test
	void roomCreateAndJoinWorks() throws Exception {
		User user1 = userRepository.save(completedUser(USER_EMAIL, "2024123456"));
		User user2 = userRepository.save(completedUser(OTHER_USER_EMAIL, "2024123457"));
		timetableEntryRepository.save(TimetableEntry.create(user1, course));
		timetableEntryRepository.save(TimetableEntry.create(user2, course));

		String request = """
			{
			  "courseId": %d,
			  "createIfAbsent": true
			}
			""".formatted(course.getId());

		mockMvc.perform(post("/api/course-chat/rooms/enter")
				.with(user(USER_EMAIL))
				.contentType(MediaType.APPLICATION_JSON)
				.content(request))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.courseName").value("웹서비스백엔드"));

		mockMvc.perform(post("/api/course-chat/rooms/enter")
				.with(user(OTHER_USER_EMAIL))
				.contentType(MediaType.APPLICATION_JSON)
				.content(request))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.courseId").value(course.getId()));

		assertThat(roomRepository.findByCourseId(course.getId())).isPresent();
		assertThat(membershipRepository.findAll()).hasSize(2);
	}

	@Test
	void messagePersistenceAndHistoryWork() throws Exception {
		User user = userRepository.save(completedUser(USER_EMAIL, "2024123456"));
		timetableEntryRepository.save(TimetableEntry.create(user, course));

		Long roomId = enterRoomAndReturnId(USER_EMAIL);
		String token = jwtTokenProvider.createAccessToken(USER_EMAIL);

		StompSession session = connect(token);
		session.subscribe("/sub/course-chat/rooms/" + roomId, new StompFrameHandler() {
			@Override
			public Type getPayloadType(StompHeaders headers) {
				return CourseChatMessageResponse.class;
			}

			@Override
			public void handleFrame(StompHeaders headers, Object payload) {
				// Subscription is established to verify the broker route; persistence is asserted below.
			}
		});

		session.send("/pub/course-chat/rooms/" + roomId + "/messages", Map.of(
			"content", "안녕하세요. 같이 과제해요!"
		));

		awaitMessagePersisted();

		assertThat(messageRepository.findAll()).hasSize(1);
		CourseChatMessage savedMessage = messageRepository.findAll().getFirst();
		assertThat(savedMessage.getContent()).isEqualTo("안녕하세요. 같이 과제해요!");

		mockMvc.perform(get("/api/course-chat/rooms/{roomId}/messages", roomId)
				.with(user(USER_EMAIL))
				.param("size", "10"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.content.length()").value(1))
			.andExpect(jsonPath("$.data.content[0].content").value("안녕하세요. 같이 과제해요!"))
			.andExpect(jsonPath("$.data.hasNext").value(false));
	}

	@Test
	void historyPaginationWorks() throws Exception {
		User user = userRepository.save(completedUser(USER_EMAIL, "2024123456"));
		timetableEntryRepository.save(TimetableEntry.create(user, course));
		Long roomId = enterRoomAndReturnId(USER_EMAIL);

		CourseChatRoom room = roomRepository.findById(roomId).orElseThrow();
		messageRepository.save(CourseChatMessage.create(room, user, "첫 번째 메시지"));
		messageRepository.save(CourseChatMessage.create(room, user, "두 번째 메시지"));

		Long latestMessageId = messageRepository.findFirstByRoomIdOrderByIdDesc(roomId).orElseThrow().getId();

		mockMvc.perform(get("/api/course-chat/rooms/{roomId}/messages", roomId)
				.with(user(USER_EMAIL))
				.param("size", "1"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.content.length()").value(1))
			.andExpect(jsonPath("$.data.hasNext").value(true))
			.andExpect(jsonPath("$.data.nextBeforeMessageId").isNumber());

		mockMvc.perform(get("/api/course-chat/rooms/{roomId}/messages", roomId)
				.with(user(USER_EMAIL))
				.param("size", "1")
				.param("beforeMessageId", String.valueOf(latestMessageId)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.content.length()").value(1));
	}

	private User completedUser(String email, String studentNumber) {
		User user = User.create(email);
		user.completeSignup("nick_" + studentNumber.substring(studentNumber.length() - 2), "Computer Science", studentNumber);
		return user;
	}

	private Long enterRoomAndReturnId(String email) throws Exception {
		String body = mockMvc.perform(post("/api/course-chat/rooms/enter")
				.with(user(email))
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "courseId": %d,
					  "createIfAbsent": true
					}
					""".formatted(course.getId())))
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();

		String marker = "\"roomId\":";
		int start = body.indexOf(marker) + marker.length();
		int end = body.indexOf(",", start);
		return Long.parseLong(body.substring(start, end));
	}

	private StompSession connect(String token) throws Exception {
		StompHeaders connectHeaders = new StompHeaders();
		connectHeaders.put("Authorization", Collections.singletonList("Bearer " + token));
		return stompClient.connectAsync(
			"ws://localhost:" + port + "/ws-stomp",
			new WebSocketHttpHeaders(),
			connectHeaders,
			new StompSessionHandlerAdapter() {}
		).get(5, TimeUnit.SECONDS);
	}

	private void awaitMessagePersisted() throws InterruptedException {
		long deadline = System.currentTimeMillis() + 5000;
		while (System.currentTimeMillis() < deadline) {
			if (messageRepository.count() > 0) {
				return;
			}
			Thread.sleep(100);
		}
		assertThat(messageRepository.count()).isGreaterThan(0);
	}
}
