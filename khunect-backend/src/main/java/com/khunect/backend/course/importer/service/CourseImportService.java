package com.khunect.backend.course.importer.service;

import com.khunect.backend.course.entity.Course;
import com.khunect.backend.course.entity.CourseSourceType;
import com.khunect.backend.course.entity.SemesterTerm;
import com.khunect.backend.course.importer.dto.CourseImportResult;
import com.khunect.backend.course.importer.dto.CourseImportRow;
import com.khunect.backend.course.repository.CourseRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CourseImportService {

	private final CourseRepository courseRepository;

	public CourseImportService(CourseRepository courseRepository) {
		this.courseRepository = courseRepository;
	}

	public CourseImportResult importRows(List<CourseImportRow> rows, CourseSourceType defaultSourceType) {
		int created = 0;
		int updated = 0;
		int skipped = 0;

		for (CourseImportRow row : rows) {
			if (row.courseCode() == null || row.courseCode().isBlank()) {
				skipped++;
				continue;
			}

			SemesterTerm semesterTerm = SemesterTerm.valueOf(row.semesterTerm().trim().toUpperCase());
			CourseSourceType sourceType = defaultSourceType;

			Course existing = courseRepository.findByCourseCode(row.courseCode().trim()).orElse(null);
			if (existing == null) {
				courseRepository.save(Course.builder()
					.courseCode(row.courseCode().trim())
					.courseName(normalize(row.courseName()))
					.professorName(normalize(row.professorName()))
					.departmentName(normalize(row.departmentName()))
					.scheduleText(normalize(row.scheduleText()))
					.classroom(normalizeNullable(row.classroom()))
					.semesterYear(row.semesterYear())
					.semesterTerm(semesterTerm)
					.sourceType(sourceType)
					.build());
				created++;
				continue;
			}

			existing.updateFromImport(
				normalize(row.courseName()),
				normalize(row.professorName()),
				normalize(row.departmentName()),
				normalize(row.scheduleText()),
				normalizeNullable(row.classroom()),
				row.semesterYear(),
				semesterTerm,
				sourceType
			);
			updated++;
		}

		return new CourseImportResult(created, updated, skipped);
	}

	private String normalize(String value) {
		return value == null ? "" : value.trim().replaceAll("\\s{2,}", " ");
	}

	private String normalizeNullable(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		return normalize(value);
	}
}
