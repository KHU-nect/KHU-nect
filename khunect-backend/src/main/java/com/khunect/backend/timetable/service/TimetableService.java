package com.khunect.backend.timetable.service;

import com.khunect.backend.common.exception.CustomException;
import com.khunect.backend.common.exception.ErrorCode;
import com.khunect.backend.course.entity.Course;
import com.khunect.backend.course.service.CourseService;
import com.khunect.backend.timetable.dto.request.AddTimetableEntryRequest;
import com.khunect.backend.timetable.dto.response.TimetableEntryResponse;
import com.khunect.backend.timetable.entity.TimetableEntry;
import com.khunect.backend.timetable.repository.TimetableEntryRepository;
import com.khunect.backend.user.entity.User;
import com.khunect.backend.user.service.UserService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class TimetableService {

	private final TimetableEntryRepository timetableEntryRepository;
	private final CourseService courseService;
	private final UserService userService;

	public TimetableService(
		TimetableEntryRepository timetableEntryRepository,
		CourseService courseService,
		UserService userService
	) {
		this.timetableEntryRepository = timetableEntryRepository;
		this.courseService = courseService;
		this.userService = userService;
	}

	@Transactional
	public TimetableEntryResponse addEntry(String email, AddTimetableEntryRequest request) {
		User user = userService.getOrCreateUser(email);
		if (!user.isSignupCompleted()) {
			throw new CustomException(ErrorCode.SIGNUP_NOT_COMPLETED);
		}

		Course course = courseService.findCourse(request.courseId());
		if (timetableEntryRepository.existsByUserIdAndCourseId(user.getId(), course.getId())) {
			throw new CustomException(ErrorCode.TIMETABLE_ENTRY_DUPLICATED);
		}

		TimetableEntry entry = timetableEntryRepository.save(TimetableEntry.create(user, course));
		return toResponse(entry);
	}

	public List<TimetableEntryResponse> getMyTimetable(String email) {
		User user = userService.getOrCreateUser(email);
		return timetableEntryRepository.findAllByUserIdWithCourse(user.getId()).stream()
			.map(this::toResponse)
			.toList();
	}

	@Transactional
	public void deleteEntry(String email, Long entryId) {
		User user = userService.getOrCreateUser(email);
		TimetableEntry entry = timetableEntryRepository.findByIdAndUserId(entryId, user.getId())
			.orElseThrow(() -> new CustomException(ErrorCode.TIMETABLE_ENTRY_NOT_FOUND));
		timetableEntryRepository.delete(entry);
	}

	private TimetableEntryResponse toResponse(TimetableEntry entry) {
		Course course = entry.getCourse();
		return new TimetableEntryResponse(
			entry.getId(),
			course.getId(),
			course.getCourseCode(),
			course.getCourseName(),
			course.getProfessorName(),
			course.getDepartmentName(),
			course.getScheduleText(),
			course.getClassroom(),
			course.getSemesterYear(),
			course.getSemesterTerm().name()
		);
	}
}
