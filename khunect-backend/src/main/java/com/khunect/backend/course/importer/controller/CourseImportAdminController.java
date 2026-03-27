package com.khunect.backend.course.importer.controller;

import com.khunect.backend.common.exception.CustomException;
import com.khunect.backend.common.exception.ErrorCode;
import com.khunect.backend.common.response.ApiResponse;
import com.khunect.backend.course.importer.dto.CourseImportResult;
import com.khunect.backend.course.importer.dto.KhuCourseCrawlRequest;
import com.khunect.backend.course.importer.service.CsvCourseImportService;
import com.khunect.backend.course.importer.service.KhuCourseCrawlerService;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.Arrays;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin/courses/import")
public class CourseImportAdminController {

	private final CsvCourseImportService csvCourseImportService;
	private final KhuCourseCrawlerService khuCourseCrawlerService;
	private final Environment environment;

	public CourseImportAdminController(
		CsvCourseImportService csvCourseImportService,
		KhuCourseCrawlerService khuCourseCrawlerService,
		Environment environment
	) {
		this.csvCourseImportService = csvCourseImportService;
		this.khuCourseCrawlerService = khuCourseCrawlerService;
		this.environment = environment;
	}

	@PostMapping(value = "/csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ApiResponse<CourseImportResult> importCsv(@RequestPart("file") MultipartFile file) throws IOException {
		ensureLocalOrDevOnly();
		return ApiResponse.success(csvCourseImportService.importCsv(file.getInputStream()));
	}

	@PostMapping("/crawler")
	public ApiResponse<CourseImportResult> importCrawler(@Valid @RequestBody KhuCourseCrawlRequest request) {
		ensureLocalOrDevOnly();
		return ApiResponse.success(khuCourseCrawlerService.crawlAndImport(
			request.keyword(),
			request.semesterYear(),
			request.semesterTerm()
		));
	}

	private void ensureLocalOrDevOnly() {
		boolean allowed = Arrays.stream(environment.getActiveProfiles())
			.anyMatch(profile -> profile.equals("local") || profile.equals("dev"));
		if (!allowed) {
			throw new CustomException(ErrorCode.FORBIDDEN, "course import admin API는 local/dev 환경에서만 사용할 수 있습니다.");
		}
	}
}
