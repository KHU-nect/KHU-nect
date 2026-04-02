package com.khunect.backend.course.repository;

import com.khunect.backend.course.entity.CourseSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseScheduleRepository extends JpaRepository<CourseSchedule, Long> {
}
