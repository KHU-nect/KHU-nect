package com.khunect.backend.course.importer.service;

import com.khunect.backend.common.exception.CustomException;
import com.khunect.backend.common.exception.ErrorCode;
import com.khunect.backend.course.entity.CourseSourceType;
import com.khunect.backend.course.importer.config.KhuCourseCrawlerProperties;
import com.khunect.backend.course.importer.dto.CourseImportResult;
import com.khunect.backend.course.importer.dto.CourseImportRow;
import java.io.IOException;
import java.util.List;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;

@Service
public class KhuCourseCrawlerService {

	private final KhuCourseCrawlerProperties properties;
	private final KhuCourseParser parser;
	private final CourseImportService courseImportService;

	public KhuCourseCrawlerService(
		KhuCourseCrawlerProperties properties,
		KhuCourseParser parser,
		CourseImportService courseImportService
	) {
		this.properties = properties;
		this.parser = parser;
		this.courseImportService = courseImportService;
	}

	public CourseImportResult crawlAndImport(String keyword, int semesterYear, String semesterTerm) {
		if (!properties.isEnabled() || properties.getBaseUrl() == null || properties.getBaseUrl().isBlank()) {
			return new CourseImportResult(0, 0, 0);
		}

		try {
			String html = fetchHtml(keyword, semesterYear, semesterTerm);
			List<CourseImportRow> rows = parser.parse(html, properties, semesterYear, semesterTerm);
			return courseImportService.importRows(rows, CourseSourceType.IMPORTED);
		} catch (IOException exception) {
			return new CourseImportResult(0, 0, 0);
		} catch (RuntimeException exception) {
			throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "크롤러 파싱에 실패했습니다.");
		}
	}

	private String fetchHtml(String keyword, int semesterYear, String semesterTerm) throws IOException {
		Connection connection = Jsoup.connect(properties.getBaseUrl() + properties.getSearchPath())
			.ignoreContentType(true)
			.method(Connection.Method.GET);

		if (properties.getKeywordParamName() != null) {
			connection.data(properties.getKeywordParamName(), keyword);
		}
		if (properties.getYearParamName() != null) {
			connection.data(properties.getYearParamName(), String.valueOf(semesterYear));
		}
		if (properties.getTermParamName() != null) {
			connection.data(properties.getTermParamName(), semesterTerm);
		}

		return connection.execute().body();
	}
}
