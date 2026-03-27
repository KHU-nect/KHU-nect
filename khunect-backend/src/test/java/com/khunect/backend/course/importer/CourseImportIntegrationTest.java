package com.khunect.backend.course.importer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.khunect.backend.course.repository.CourseRepository;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
class CourseImportIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private CourseRepository courseRepository;

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
		jdbcTemplate.execute("delete from course");
	}

	@Test
	void csvImportAdminApiWorksAndUpsertsByCourseCode() throws Exception {
		MockMultipartFile file = new MockMultipartFile(
			"file",
			"courses.csv",
			"text/csv",
			("""
				courseCode,courseName,professorName,departmentName,scheduleText,classroom,semesterYear,semesterTerm,sourceType
				CSE999,테스트과목,김교수,컴퓨터공학과,"월 09:00-10:30, 수 09:00-10:30",전자정보대 999,2026,FIRST,IMPORTED
				CSE999,테스트과목수정,김교수,컴퓨터공학과,"월 09:00-10:30, 수 09:00-10:30",전자정보대 1000,2026,FIRST,IMPORTED
				""").getBytes(StandardCharsets.UTF_8)
		);

		mockMvc.perform(multipart("/api/admin/courses/import/csv")
				.file(file)
				.with(user("admin@khu.ac.kr")))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.createdCount").value(1))
			.andExpect(jsonPath("$.data.updatedCount").value(1));

		assertThat(courseRepository.count()).isEqualTo(1);
		assertThat(courseRepository.findByCourseCode("CSE999")).isPresent();
		assertThat(courseRepository.findByCourseCode("CSE999").orElseThrow().getCourseName()).isEqualTo("테스트과목수정");
	}
}
