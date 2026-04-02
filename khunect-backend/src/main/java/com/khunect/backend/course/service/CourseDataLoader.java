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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
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
		if (courseRepository.existsBySourceType(CourseSourceType.SEEDED)) {
			log.info("[CourseDataLoader] SEEDED 데이터가 이미 존재합니다. 임포트를 건너뜁니다. (재시드가 필요하면 SEEDED 강의를 모두 삭제 후 재시작하세요.)");
			return;
		}

		ClassPathResource resource = new ClassPathResource(JSON_PATH);
		List<CourseDataImportDto> dtos;
		try (InputStream is = resource.getInputStream()) {
			dtos = objectMapper.readValue(is, new TypeReference<>() {});
		}

		List<Course> courses = new ArrayList<>();
		Set<String> seenCourseCodes = new LinkedHashSet<>();
		for (CourseDataImportDto dto : dtos) {
			if (!seenCourseCodes.add(dto.getCourseCode())) {
				log.debug("[CourseDataLoader] 중복 courseCode 건너뜀: {}", dto.getCourseCode());
				continue;
			}
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
		log.info("[CourseDataLoader] {} 건 임포트 완료 (JSON 총 {} 건 중 중복 {} 건 제외)",
			courses.size(), dtos.size(), dtos.size() - courses.size());
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
