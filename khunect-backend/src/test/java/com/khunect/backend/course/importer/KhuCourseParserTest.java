package com.khunect.backend.course.importer;

import static org.assertj.core.api.Assertions.assertThat;

import com.khunect.backend.course.importer.config.KhuCourseCrawlerProperties;
import com.khunect.backend.course.importer.dto.CourseImportRow;
import com.khunect.backend.course.importer.service.KhuCourseParser;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

class KhuCourseParserTest {

	private final KhuCourseParser parser = new KhuCourseParser();

	@Test
	void parseFixtureHtml() throws Exception {
		String html = Files.readString(Path.of("src/test/resources/fixtures/khu-course-search-sample.html"));

		KhuCourseCrawlerProperties properties = new KhuCourseCrawlerProperties();
		properties.setRowSelector("table.course-search tbody tr");
		properties.setCourseCodeSelector("td.course-code");
		properties.setCourseNameSelector("td.course-name");
		properties.setProfessorNameSelector("td.professor-name");
		properties.setDepartmentNameSelector("td.department-name");
		properties.setScheduleTextSelector("td.schedule-text");
		properties.setClassroomSelector("td.classroom");

		List<CourseImportRow> rows = parser.parse(html, properties, 2026, "FIRST");

		assertThat(rows).hasSize(2);
		assertThat(rows.getFirst().courseCode()).isEqualTo("KHU101");
		assertThat(rows.getFirst().courseName()).isEqualTo("세계와 시민");
		assertThat(rows.get(1).professorName()).isEqualTo("박현자");
	}
}
