package com.khunect.backend.match;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.khunect.backend.chat.direct.entity.DirectChatMessage;
import com.khunect.backend.chat.direct.entity.DirectChatRoom;
import com.khunect.backend.chat.direct.repository.DirectChatMessageRepository;
import com.khunect.backend.chat.direct.repository.DirectChatRoomRepository;
import com.khunect.backend.common.security.JwtTokenProvider;
import com.khunect.backend.match.entity.MatchPost;
import com.khunect.backend.match.repository.MatchPostRepository;
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
class MatchDirectChatIntegrationTest {

	private static final String AUTHOR_EMAIL = "author@khu.ac.kr";
	private static final String ACCEPTOR_EMAIL = "acceptor@khu.ac.kr";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private MatchPostRepository matchPostRepository;

	@Autowired
	private DirectChatRoomRepository directChatRoomRepository;

	@Autowired
	private DirectChatMessageRepository directChatMessageRepository;

	@Autowired
	private JwtTokenProvider jwtTokenProvider;

	@LocalServerPort
	private int port;

	private WebSocketStompClient stompClient;

	@BeforeEach
	void setUp() {
		directChatMessageRepository.deleteAll();
		directChatRoomRepository.deleteAll();
		matchPostRepository.deleteAll();
		userRepository.deleteAll();

		userRepository.save(completedUser(AUTHOR_EMAIL, "2024123001"));
		userRepository.save(completedUser(ACCEPTOR_EMAIL, "2024123002"));

		stompClient = new WebSocketStompClient(new StandardWebSocketClient());
		stompClient.setMessageConverter(new MappingJackson2MessageConverter());
	}

	@AfterEach
	void tearDown() {
		if (stompClient != null) {
			stompClient.stop();
		}
	}

	@Test
	void matchPostCrudWorks() throws Exception {
		Long postId = createMatchPost(AUTHOR_EMAIL, "도서관 앞", "저녁", "같이 공부할 분");

		mockMvc.perform(get("/api/match-posts/{id}", postId)
				.with(user(AUTHOR_EMAIL)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.content").value("같이 공부할 분"));

		mockMvc.perform(patch("/api/match-posts/{id}", postId)
				.with(user(AUTHOR_EMAIL))
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "preferredTimeText": "주중 저녁",
					  "locationText": "중앙도서관",
					  "content": "같이 백엔드 과제할 분",
					  "category": "study"
					}
					"""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.locationText").value("중앙도서관"))
			.andExpect(jsonPath("$.data.content").value("같이 백엔드 과제할 분"));

		mockMvc.perform(delete("/api/match-posts/{id}", postId)
				.with(user(AUTHOR_EMAIL)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true));

		MatchPost deleted = matchPostRepository.findById(postId).orElseThrow();
		assertThat(deleted.getStatus().name()).isEqualTo("CANCELED");
	}

	@Test
	void acceptSuccessAndFailuresAndRoomReuseWork() throws Exception {
		Long firstPostId = createMatchPost(AUTHOR_EMAIL, "청운관", "수업 후", "세계와 시민 스터디");

		mockMvc.perform(post("/api/match-posts/{id}/accept", firstPostId)
				.with(user(AUTHOR_EMAIL)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error.code").value("MATCH-400-1"));

		String firstAcceptBody = mockMvc.perform(post("/api/match-posts/{id}/accept", firstPostId)
				.with(user(ACCEPTOR_EMAIL)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.status").value("ACCEPTED"))
			.andReturn()
			.getResponse()
			.getContentAsString();

		Long firstRoomId = extractLong(firstAcceptBody, "\"directRoomId\":");

		mockMvc.perform(post("/api/match-posts/{id}/accept", firstPostId)
				.with(user(ACCEPTOR_EMAIL)))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.error.code").value("MATCH-409-1"));

		Long secondPostId = createMatchPost(ACCEPTOR_EMAIL, "전자정보대", "금요일", "자료구조 복습");
		String secondAcceptBody = mockMvc.perform(post("/api/match-posts/{id}/accept", secondPostId)
				.with(user(AUTHOR_EMAIL)))
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();

		Long secondRoomId = extractLong(secondAcceptBody, "\"directRoomId\":");
		assertThat(secondRoomId).isEqualTo(firstRoomId);

		mockMvc.perform(get("/api/direct-chat/rooms/me")
				.with(user(AUTHOR_EMAIL)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.length()").value(1))
			.andExpect(jsonPath("$.data[0].opponentNickname").isNotEmpty());
	}

	@Test
	void directMessagePersistenceAndHistoryWork() throws Exception {
		Long postId = createMatchPost(AUTHOR_EMAIL, "정문", "오후", "같이 프로젝트할 분");
		String acceptBody = mockMvc.perform(post("/api/match-posts/{id}/accept", postId)
				.with(user(ACCEPTOR_EMAIL)))
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();

		Long roomId = extractLong(acceptBody, "\"directRoomId\":");
		String token = jwtTokenProvider.createAccessToken(AUTHOR_EMAIL);

		StompSession session = connect(token);
		session.subscribe("/sub/direct-chat/rooms/" + roomId, new StompFrameHandler() {
			@Override
			public Type getPayloadType(StompHeaders headers) {
				return Object.class;
			}

			@Override
			public void handleFrame(StompHeaders headers, Object payload) {
			}
		});

		session.send("/pub/direct-chat/rooms/" + roomId + "/messages", Map.of(
			"content", "안녕하세요. 매칭 감사합니다."
		));

		awaitDirectMessagePersisted();

		assertThat(directChatMessageRepository.findAll()).hasSize(1);
		DirectChatMessage message = directChatMessageRepository.findAll().getFirst();
		assertThat(message.getContent()).isEqualTo("안녕하세요. 매칭 감사합니다.");

		mockMvc.perform(get("/api/direct-chat/rooms/{roomId}/messages", roomId)
				.with(user(AUTHOR_EMAIL))
				.param("size", "10"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.content.length()").value(1))
			.andExpect(jsonPath("$.data.content[0].content").value("안녕하세요. 매칭 감사합니다."));
	}

	private User completedUser(String email, String studentNumber) {
		User user = User.create(email);
		user.completeSignup("nick_" + studentNumber.substring(studentNumber.length() - 2), "Computer Science", studentNumber);
		return user;
	}

	private Long createMatchPost(String email, String location, String preferredTime, String content) throws Exception {
		String body = mockMvc.perform(post("/api/match-posts")
				.with(user(email))
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "preferredTimeText": "%s",
					  "locationText": "%s",
					  "content": "%s",
					  "category": "study"
					}
					""".formatted(preferredTime, location, content)))
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();
		return extractLong(body, "\"id\":");
	}

	private Long extractLong(String body, String marker) {
		int start = body.indexOf(marker) + marker.length();
		int end = body.indexOf(",", start);
		if (end < 0) {
			end = body.indexOf("}", start);
		}
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

	private void awaitDirectMessagePersisted() throws InterruptedException {
		long deadline = System.currentTimeMillis() + 5000;
		while (System.currentTimeMillis() < deadline) {
			if (directChatMessageRepository.count() > 0) {
				return;
			}
			Thread.sleep(100);
		}
		assertThat(directChatMessageRepository.count()).isGreaterThan(0);
	}
}
