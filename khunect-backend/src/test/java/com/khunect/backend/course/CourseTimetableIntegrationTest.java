package com.khunect.backend.course;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.khunect.backend.course.entity.Course;
import com.khunect.backend.course.entity.CourseSourceType;
import com.khunect.backend.course.entity.SemesterTerm;
import com.khunect.backend.course.repository.CourseRepository;
import com.khunect.backend.timetable.repository.TimetableEntryRepository;
import com.khunect.backend.user.entity.User;
import com.khunect.backend.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CourseTimetableIntegrationTest {

	private static final String USER_EMAIL = "timetable@khu.ac.kr";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private CourseRepository courseRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private TimetableEntryRepository timetableEntryRepository;

	private Course course1;
	private Course course2;

	@BeforeEach
	void setUp() {
		timetableEntryRepository.deleteAll();
		courseRepository.deleteAll();
		userRepository.deleteAll();

		course1 = courseRepository.save(Course.builder()
			.courseCode("ART101")
			.courseName("도자성형1")
			.professorName("김도예")
			.departmentName("예술디자인대학")
			.scheduleText("월 09:00-11:45")
			.classroom("미술관 201")
			.semesterYear(2026)
			.semesterTerm(SemesterTerm.FIRST)
			.sourceType(CourseSourceType.MANUAL)
			.build());

		course2 = courseRepository.save(Course.builder()
			.courseCode("GEC110")
			.courseName("세계와 시민")
			.professorName("정민우")
			.departmentName("후마니타스칼리지")
			.scheduleText("화 10:30-12:00")
			.classroom("청운관 302")
			.semesterYear(2026)
			.semesterTerm(SemesterTerm.FIRST)
			.sourceType(CourseSourceType.MANUAL)
			.build());
	}

	@Test
	void searchCoursesByKeyword() throws Exception {
		mockMvc.perform(get("/api/courses")
				.param("keyword", "도자")
				.param("page", "0")
				.param("size", "10"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.data.content.length()").value(1))
			.andExpect(jsonPath("$.data.content[0].courseCode").value("ART101"))
			.andExpect(jsonPath("$.data.content[0].courseName").value("도자성형1"));
	}

	@Test
	void getCourseDetail() throws Exception {
		mockMvc.perform(get("/api/courses/{courseId}", course2.getId()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.data.courseCode").value("GEC110"))
			.andExpect(jsonPath("$.data.courseName").value("세계와 시민"))
			.andExpect(jsonPath("$.data.professorName").value("정민우"));
	}

	@Test
	void timetableAddSuccessAndDuplicateFail() throws Exception {
		User user = User.create(USER_EMAIL);
		user.completeSignup("khunect_user", "Computer Science", "2024123456");
		userRepository.save(user);

		String requestBody = """
			{
			  "courseId": %d
			}
			""".formatted(course1.getId());

		mockMvc.perform(post("/api/timetable")
				.with(user(USER_EMAIL))
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestBody))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.courseId").value(course1.getId()))
			.andExpect(jsonPath("$.data.courseName").value("도자성형1"));

		mockMvc.perform(post("/api/timetable")
				.with(user(USER_EMAIL))
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestBody))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.success").value(false))
			.andExpect(jsonPath("$.error.code").value("TIMETABLE-409-1"));
	}

	@Test
	void timetableGetAndDeleteWorks() throws Exception {
		User user = User.create(USER_EMAIL);
		user.completeSignup("khunect_user", "Computer Science", "2024123456");
		userRepository.save(user);

		mockMvc.perform(post("/api/timetable")
				.with(user(USER_EMAIL))
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "courseId": %d
					}
					""".formatted(course1.getId())))
			.andExpect(status().isOk());

		mockMvc.perform(post("/api/timetable")
				.with(user(USER_EMAIL))
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "courseId": %d
					}
					""".formatted(course2.getId())))
			.andExpect(status().isOk());

		mockMvc.perform(get("/api/timetable/me")
				.with(user(USER_EMAIL)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.length()").value(2));

		Long entryId = timetableEntryRepository.findAll().getFirst().getId();

		mockMvc.perform(delete("/api/timetable/{entryId}", entryId)
				.with(user(USER_EMAIL)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true));

		mockMvc.perform(get("/api/timetable/me")
				.with(user(USER_EMAIL)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.length()").value(1));
	}

	@Test
	void timetableAddFailsWhenSignupNotCompleted() throws Exception {
		userRepository.save(User.create(USER_EMAIL));

		mockMvc.perform(post("/api/timetable")
				.with(user(USER_EMAIL))
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "courseId": %d
					}
					""".formatted(course1.getId())))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.success").value(false))
			.andExpect(jsonPath("$.error.code").value("USER-403-1"));
	}
}
