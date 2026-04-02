package com.khunect.backend.course.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.khunect.backend.course.dto.CourseDataImportDto;
import com.khunect.backend.course.entity.Course;
import com.khunect.backend.course.entity.CourseSchedule;
import com.khunect.backend.course.entity.CourseSourceType;
import com.khunect.backend.course.entity.SemesterTerm;
import com.khunect.backend.course.repository.CourseRepository;
import java.io.InputStream;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class CourseDataLoader implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(CourseDataLoader.class);
	private static final String JSON_PATH = "data/수강데이터_structured.json";
	private static final int SEEDED_YEAR = 2026;
	private static final SemesterTerm SEEDED_TERM = SemesterTerm.FIRST;

	private final CourseRepository courseRepository;
	private final ObjectMapper objectMapper;

	public CourseDataLoader(CourseRepository courseRepository, ObjectMapper objectMapper) {
		this.courseRepository = courseRepository;
		this.objectMapper = objectMapper;
	}

	@Override
	@Transactional
	public void run(ApplicationArguments args) throws Exception {
		if (courseRepository.countBySourceType(CourseSourceType.SEEDED) >= 2000) {
			log.info("[CourseDataLoader] SEEDED 데이터가 이미 존재합니다. 임포트를 건너뜁니다.");
			return;
		}

		ClassPathResource resource = new ClassPathResource(JSON_PATH);
		List<CourseDataImportDto> dtos;
		try (InputStream is = resource.getInputStream()) {
			dtos = objectMapper.readValue(is, new TypeReference<>() {});
		}

		List<Course> courses = new ArrayList<>();
		for (CourseDataImportDto dto : dtos) {
			String professorName = normalizeProfessor(dto.getProfessor());
			String scheduleText = dto.getScheduleRaw() != null ? dto.getScheduleRaw() : "";
			String firstClassroom = extractFirstClassroom(dto);
			boolean isOnline = dto.getIsOnline() != null && dto.getIsOnline();

			Course course = Course.builder()
				.courseCode(dto.getCourseCode())
				.courseName(dto.getCourseName())
				.professorName(professorName)
				.departmentName(dto.getDepartment())
				.scheduleText(scheduleText)
				.classroom(firstClassroom)
				.semesterYear(SEEDED_YEAR)
				.semesterTerm(SEEDED_TERM)
				.sourceType(CourseSourceType.SEEDED)
				.college(dto.getCollege())
				.lectureCd(dto.getLectureCd())
				.isOnline(isOnline)
				.build();

			if (dto.getSchedules() != null) {
				for (CourseDataImportDto.ScheduleSlotDto slot : dto.getSchedules()) {
					if (slot.getStartTime() == null || slot.getEndTime() == null) {
						continue;
					}
					CourseSchedule schedule = CourseSchedule.builder()
						.course(course)
						.day(slot.getDay())
						.startTime(LocalTime.parse(slot.getStartTime()))
						.endTime(LocalTime.parse(slot.getEndTime()))
						.startMinutes(slot.getStartMinutes())
						.endMinutes(slot.getEndMinutes())
						.classroom(slot.getClassroom())
						.build();
					course.getSchedules().add(schedule);
				}
			}

			courses.add(course);
		}

		courseRepository.saveAll(courses);
		log.info("[CourseDataLoader] {} 건 임포트 완료", courses.size());
	}

	private String normalizeProfessor(String professor) {
		if (professor == null || professor.equals("..") || professor.isBlank()) {
			return null;
		}
		return professor;
	}

	private String extractFirstClassroom(CourseDataImportDto dto) {
		if (dto.getSchedules() == null || dto.getSchedules().isEmpty()) {
			return null;
		}
		return dto.getSchedules().get(0).getClassroom();
	}
}
