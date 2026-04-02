package com.khunect.backend.course.service;

import com.khunect.backend.common.exception.CustomException;
import com.khunect.backend.common.exception.ErrorCode;
import com.khunect.backend.course.dto.response.CourseDetailResponse;
import com.khunect.backend.course.dto.response.CourseScheduleSlotResponse;
import com.khunect.backend.course.dto.response.CourseSearchResponse;
import com.khunect.backend.course.dto.response.CourseSummaryResponse;
import com.khunect.backend.course.dto.response.CourseWithSchedulesResponse;
import com.khunect.backend.course.entity.Course;
import com.khunect.backend.course.entity.SemesterTerm;
import com.khunect.backend.course.repository.CourseRepository;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CourseService {

	private static final int CURRENT_YEAR = 2026;
	private static final SemesterTerm CURRENT_TERM = SemesterTerm.FIRST;

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

	public List<CourseWithSchedulesResponse> searchByType(String type, String keyword) {
		List<Course> courses;
		if ("professor".equals(type)) {
			courses = courseRepository.findByProfessorNameContainingAndSemesterYearAndSemesterTerm(
				keyword, CURRENT_YEAR, CURRENT_TERM);
		} else {
			courses = courseRepository.findByCourseNameContainingAndSemesterYearAndSemesterTerm(
				keyword, CURRENT_YEAR, CURRENT_TERM);
		}
		return courses.stream().map(this::toWithSchedulesResponse).toList();
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

	private CourseWithSchedulesResponse toWithSchedulesResponse(Course course) {
		List<CourseScheduleSlotResponse> scheduleSlots = course.getSchedules().stream()
			.map(s -> new CourseScheduleSlotResponse(
				s.getDay(),
				s.getStartTime().toString(),
				s.getEndTime().toString(),
				s.getStartMinutes(),
				s.getEndMinutes(),
				s.getClassroom()
			))
			.toList();
		return new CourseWithSchedulesResponse(
			course.getId(),
			course.getCourseCode(),
			course.getCourseName(),
			course.getProfessorName(),
			course.getDepartmentName(),
			course.getCollege(),
			course.isOnline(),
			course.getScheduleText(),
			scheduleSlots
		);
	}
}
