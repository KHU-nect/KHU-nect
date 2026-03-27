package com.khunect.backend.timetable.repository;

import com.khunect.backend.timetable.entity.TimetableEntry;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TimetableEntryRepository extends JpaRepository<TimetableEntry, Long> {

	boolean existsByUserIdAndCourseId(Long userId, Long courseId);

	Optional<TimetableEntry> findByIdAndUserId(Long id, Long userId);

	long countByUserId(Long userId);

	@Query("""
		select te
		from TimetableEntry te
		join fetch te.course c
		where te.user.id = :userId
		order by c.semesterYear desc, c.semesterTerm asc, c.courseName asc
		""")
	List<TimetableEntry> findAllByUserIdWithCourse(Long userId);
}
