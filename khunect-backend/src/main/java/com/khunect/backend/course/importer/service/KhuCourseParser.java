package com.khunect.backend.course.importer.service;

import com.khunect.backend.course.importer.config.KhuCourseCrawlerProperties;
import com.khunect.backend.course.importer.dto.CourseImportRow;
import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

@Component
public class KhuCourseParser {

	public List<CourseImportRow> parse(
		String html,
		KhuCourseCrawlerProperties properties,
		int semesterYear,
		String semesterTerm
	) {
		Document document = Jsoup.parse(html);
		return document.select(properties.getRowSelector()).stream()
			.map(row -> new CourseImportRow(
				text(row, properties.getCourseCodeSelector()),
				text(row, properties.getCourseNameSelector()),
				text(row, properties.getProfessorNameSelector()),
				text(row, properties.getDepartmentNameSelector()),
				text(row, properties.getScheduleTextSelector()),
				text(row, properties.getClassroomSelector()),
				semesterYear,
				semesterTerm,
				"IMPORTED"
			))
			.filter(course -> !course.courseCode().isBlank())
			.toList();
	}

	private String text(Element row, String selector) {
		Element element = row.selectFirst(selector);
		return element == null ? "" : element.text().trim();
	}
}
