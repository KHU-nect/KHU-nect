package com.khunect.backend.course.service;

import com.khunect.backend.common.exception.CustomException;
import com.khunect.backend.common.exception.ErrorCode;
import com.khunect.backend.course.dto.response.CourseDetailResponse;
import com.khunect.backend.course.dto.response.CourseSearchResponse;
import com.khunect.backend.course.dto.response.CourseSummaryResponse;
import com.khunect.backend.course.entity.Course;
import com.khunect.backend.course.repository.CourseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CourseService {

	private final CourseRepository courseRepository;

	public CourseService(CourseRepository courseRepository) {
		this.courseRepository = courseRepository;
	}

	public CourseSearchResponse search(String keyword, int page, int size) {
		Page<Course> result = courseRepository.search(normalizeKeyword(keyword), PageRequest.of(page, size));
		return new CourseSearchResponse(
			result.getContent().stream().map(this::toSummaryResponse).toList(),
			result.getNumber(),
			result.getSize(),
			result.getTotalElements(),
			result.getTotalPages(),
			result.hasNext()
		);
	}

	public CourseDetailResponse getById(Long courseId) {
		Course course = courseRepository.findById(courseId)
			.orElseThrow(() -> new CustomException(ErrorCode.COURSE_NOT_FOUND));
		return toDetailResponse(course);
	}

	public Course findCourse(Long courseId) {
		return courseRepository.findById(courseId)
			.orElseThrow(() -> new CustomException(ErrorCode.COURSE_NOT_FOUND));
	}

	private String normalizeKeyword(String keyword) {
		if (keyword == null) {
			return null;
		}
		String trimmed = keyword.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}

	private CourseSummaryResponse toSummaryResponse(Course course) {
		return new CourseSummaryResponse(
			course.getId(),
			course.getCourseCode(),
			course.getCourseName(),
			course.getProfessorName(),
			course.getDepartmentName(),
			course.getScheduleText(),
			course.getClassroom(),
			course.getSemesterYear(),
			course.getSemesterTerm().name(),
			course.getSourceType().name()
		);
	}

	private CourseDetailResponse toDetailResponse(Course course) {
		return new CourseDetailResponse(
			course.getId(),
			course.getCourseCode(),
			course.getCourseName(),
			course.getProfessorName(),
			course.getDepartmentName(),
			course.getScheduleText(),
			course.getClassroom(),
			course.getSemesterYear(),
			course.getSemesterTerm().name(),
			course.getSourceType().name()
		);
	}
}
