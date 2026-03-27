package com.khunect.backend.user;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.khunect.backend.interest.entity.Interest;
import com.khunect.backend.interest.entity.UserInterest;
import com.khunect.backend.interest.repository.InterestRepository;
import com.khunect.backend.interest.repository.UserInterestRepository;
import com.khunect.backend.user.entity.User;
import com.khunect.backend.user.repository.UserRepository;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserInterestMyPageIntegrationTest {

	private static final String USER_EMAIL = "student@khu.ac.kr";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private InterestRepository interestRepository;

	@Autowired
	private UserInterestRepository userInterestRepository;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@BeforeEach
	void setUp() {
		jdbcTemplate.execute("delete from direct_chat_message");
		jdbcTemplate.execute("delete from direct_chat_room");
		jdbcTemplate.execute("delete from match_post");
		jdbcTemplate.execute("delete from course_chat_message");
		jdbcTemplate.execute("delete from course_chat_room_membership");
		jdbcTemplate.execute("delete from course_chat_room");
		jdbcTemplate.execute("delete from timetable_entry");
		jdbcTemplate.execute("delete from user_interest");
		jdbcTemplate.execute("delete from interest");
		jdbcTemplate.execute("delete from users");
	}

	@Test
	void unauthenticatedAccessFails() throws Exception {
		mockMvc.perform(get("/api/users/me"))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.success").value(false))
			.andExpect(jsonPath("$.error.code").value("AUTH-401"));
	}

	@Test
	void signupCompletionSucceeds() throws Exception {
		String requestBody = """
			{
			  "nickname": "khunect_1",
			  "major": "Computer Science",
			  "studentNumber": "2024123456"
			}
			""";

		mockMvc.perform(post("/api/users/me/signup-completion")
				.with(user(USER_EMAIL))
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestBody.getBytes(StandardCharsets.UTF_8)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.data.email").value(USER_EMAIL))
			.andExpect(jsonPath("$.data.signupCompleted").value(true))
			.andExpect(jsonPath("$.data.studentNumber").value("2024123456"));
	}

	@Test
	void signupCompletionFailsWhenAlreadyCompleted() throws Exception {
		User user = User.create(USER_EMAIL);
		user.completeSignup("khunect_1", "Computer Science", "2024123456");
		userRepository.save(user);

		String requestBody = """
			{
			  "nickname": "khunect_2",
			  "major": "Business",
			  "studentNumber": "2024999999"
			}
			""";

		mockMvc.perform(post("/api/users/me/signup-completion")
				.with(user(USER_EMAIL))
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestBody))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.success").value(false))
			.andExpect(jsonPath("$.error.code").value("USER-409-1"));
	}

	@Test
	void interestAddRemoveAndListWorks() throws Exception {
		mockMvc.perform(post("/api/interests/me")
				.with(user(USER_EMAIL))
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "name": "Board Game"
					}
					"""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.name").value("Board Game"));

		Interest interest = interestRepository.findByNameIgnoreCase("Board Game").orElseThrow();

		mockMvc.perform(get("/api/interests/me")
				.with(user(USER_EMAIL)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.length()").value(1))
			.andExpect(jsonPath("$.data[0].name").value("Board Game"));

		mockMvc.perform(delete("/api/interests/me/{interestId}", interest.getId())
				.with(user(USER_EMAIL)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true));

		mockMvc.perform(get("/api/interests/me")
				.with(user(USER_EMAIL)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.length()").value(0));
	}

	@Test
	void summaryResponseIsReturned() throws Exception {
		User user = User.create(USER_EMAIL);
		user.completeSignup("khunect_1", "Computer Science", "2024123456");
		userRepository.save(user);

		Interest interest = interestRepository.save(Interest.create("Music"));
		userInterestRepository.save(UserInterest.create(user, interest));

		mockMvc.perform(get("/api/mypage/summary")
				.with(user(USER_EMAIL)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.data.nickname").value("khunect_1"))
			.andExpect(jsonPath("$.data.major").value("Computer Science"))
			.andExpect(jsonPath("$.data.maskedStudentNumber").value("2024****56"))
			.andExpect(jsonPath("$.data.point").value(0))
			.andExpect(jsonPath("$.data.level").value(1))
			.andExpect(jsonPath("$.data.registeredCourseCount").value(0))
			.andExpect(jsonPath("$.data.successfulMatchCount").value(0))
			.andExpect(jsonPath("$.data.helpedCount").value(0))
			.andExpect(jsonPath("$.data.weeklyStats.courseChatMessageCount").value(0))
			.andExpect(jsonPath("$.data.weeklyStats.directChatMessageCount").value(0))
			.andExpect(jsonPath("$.data.weeklyStats.matchPostCreatedCount").value(0))
			.andExpect(jsonPath("$.data.weeklyStats.acceptedMatchCount").value(0));
	}

	@Test
	void updateProfileChangesOnlyNicknameAndMajor() throws Exception {
		User user = User.create(USER_EMAIL);
		user.completeSignup("old_name", "Old Major", "2024123456");
		userRepository.save(user);

		mockMvc.perform(patch("/api/users/me")
				.with(user(USER_EMAIL))
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "nickname": "new_name",
					  "major": "New Major"
					}
					"""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.nickname").value("new_name"))
			.andExpect(jsonPath("$.data.major").value("New Major"))
			.andExpect(jsonPath("$.data.studentNumber").value("2024123456"));
	}
}
