package com.khunect.backend.timetable.repository;

import com.khunect.backend.course.entity.Course;
import com.khunect.backend.timetable.entity.TimetableEntry;
import com.khunect.backend.user.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TimetableEntryRepository extends JpaRepository<TimetableEntry, Long> {

	boolean existsByUserIdAndCourseId(Long userId, Long courseId);

	Optional<TimetableEntry> findByIdAndUserId(Long id, Long userId);

	long countByUserId(Long userId);

	long countByCourseId(Long courseId);

	@Query("""
		select te
		from TimetableEntry te
		join fetch te.course c
		where te.user.id = :userId
		order by c.semesterYear desc, c.semesterTerm asc, c.courseName asc
		""")
	List<TimetableEntry> findAllByUserIdWithCourse(Long userId);

	@Query("""
		select distinct te.user
		from TimetableEntry te
		where te.user.id != :userId
		and te.user.signupCompleted = true
		and (:interestId is null or exists (
		    select 1 from UserInterest ui where ui.user = te.user and ui.interest.id = :interestId
		))
		""")
	List<User> findUsersWithTimetable(Long userId, Long interestId);

	@Query("""
		select distinct te.user
		from TimetableEntry te
		where te.user.id != :userId
		and te.user.signupCompleted = true
		and te.course.id in (
		    select te2.course.id from TimetableEntry te2 where te2.user.id = :userId
		)
		and (:interestId is null or exists (
		    select 1 from UserInterest ui where ui.user = te.user and ui.interest.id = :interestId
		))
		""")
	List<User> findUsersWithSameCourse(Long userId, Long interestId);

	@Query("""
		select te.course
		from TimetableEntry te
		where te.user.id = :userId
		and te.course.id in (
		    select te2.course.id from TimetableEntry te2 where te2.user.id = :targetUserId
		)
		""")
	List<Course> findCommonCourses(Long userId, Long targetUserId);
}
