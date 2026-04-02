package com.khunect.backend.course.repository;

import com.khunect.backend.course.entity.Course;
import com.khunect.backend.course.entity.CourseSourceType;
import com.khunect.backend.course.entity.SemesterTerm;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CourseRepository extends JpaRepository<Course, Long> {

	Optional<Course> findByCourseCode(String courseCode);

	boolean existsBySourceType(CourseSourceType sourceType);

	long countBySourceType(CourseSourceType sourceType);

	@Query("""
		select c
		from Course c
		where :keyword is null
		   or lower(c.courseName) like lower(concat('%', :keyword, '%'))
		   or lower(c.professorName) like lower(concat('%', :keyword, '%'))
		   or lower(c.courseCode) like lower(concat('%', :keyword, '%'))
		order by c.semesterYear desc, c.semesterTerm asc, c.courseName asc
		""")
	Page<Course> search(String keyword, Pageable pageable);

	List<Course> findByCourseNameContainingAndSemesterYearAndSemesterTerm(
		String keyword, int semesterYear, SemesterTerm semesterTerm);

	List<Course> findByProfessorNameContainingAndSemesterYearAndSemesterTerm(
		String keyword, int semesterYear, SemesterTerm semesterTerm);
}
