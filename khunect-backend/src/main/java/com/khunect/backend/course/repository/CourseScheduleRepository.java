package com.khunect.backend.course.repository;

import com.khunect.backend.course.entity.CourseSchedule;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CourseScheduleRepository extends JpaRepository<CourseSchedule, Long> {

	@Query("""
		select cs
		from CourseSchedule cs
		join cs.course c
		join TimetableEntry te on te.course = c
		where te.user.id = :userId
		""")
	List<CourseSchedule> findAllByUserId(Long userId);
}
