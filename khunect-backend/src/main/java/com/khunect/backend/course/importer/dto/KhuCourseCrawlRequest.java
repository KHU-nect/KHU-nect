package com.khunect.backend.course.importer.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record KhuCourseCrawlRequest(
	@NotBlank(message = "검색어는 필수입니다.")
	String keyword,

	@NotNull(message = "학년도는 필수입니다.")
	@Min(value = 2020, message = "학년도는 2020 이상이어야 합니다.")
	@Max(value = 2100, message = "학년도는 2100 이하여야 합니다.")
	Integer semesterYear,

	@NotBlank(message = "학기는 필수입니다.")
	String semesterTerm
) {
}
