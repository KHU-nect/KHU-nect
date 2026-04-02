package com.khunect.backend.course.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "course_schedules")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CourseSchedule {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "course_id", nullable = false)
	private Course course;

	@Column(nullable = false, length = 3)
	private String day;

	@Column(nullable = false)
	private LocalTime startTime;

	@Column(nullable = false)
	private LocalTime endTime;

	@Column(nullable = false)
	private int startMinutes;

	@Column(nullable = false)
	private int endMinutes;

	@Column(length = 50)
	private String classroom;

	@Builder
	private CourseSchedule(
		Course course,
		String day,
		LocalTime startTime,
		LocalTime endTime,
		int startMinutes,
		int endMinutes,
		String classroom
	) {
		this.course = course;
		this.day = day;
		this.startTime = startTime;
		this.endTime = endTime;
		this.startMinutes = startMinutes;
		this.endMinutes = endMinutes;
		this.classroom = classroom;
	}
}
