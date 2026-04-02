package com.khunect.backend.course.controller;

import com.khunect.backend.common.response.ApiResponse;
import com.khunect.backend.course.dto.response.CourseDetailResponse;
import com.khunect.backend.course.dto.response.CourseSearchResponse;
import com.khunect.backend.course.dto.response.CourseWithSchedulesResponse;
import com.khunect.backend.course.service.CourseService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/courses")
public class CourseController {

	private final CourseService courseService;

	public CourseController(CourseService courseService) {
		this.courseService = courseService;
	}

	@GetMapping
	public ApiResponse<CourseSearchResponse> search(
		@RequestParam(required = false) String keyword,
		@RequestParam(defaultValue = "0") @Min(0) int page,
		@RequestParam(defaultValue = "10") @Min(1) @Max(100) int size
	) {
		return ApiResponse.success(courseService.search(keyword, page, size));
	}

	@GetMapping("/search")
	public ApiResponse<List<CourseWithSchedulesResponse>> searchByType(
		@RequestParam(defaultValue = "course_name") String type,
		@RequestParam @NotBlank String keyword
	) {
		return ApiResponse.success(courseService.searchByType(type, keyword));
	}

	@GetMapping("/{courseId}")
	public ApiResponse<CourseDetailResponse> getCourse(@PathVariable Long courseId) {
		return ApiResponse.success(courseService.getById(courseId));
	}
}
