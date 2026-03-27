package com.khunect.backend.course.entity;

import com.khunect.backend.common.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "course")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Course extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true, length = 30)
	private String courseCode;

	@Column(nullable = false, length = 100)
	private String courseName;

	@Column(nullable = false, length = 50)
	private String professorName;

	@Column(nullable = false, length = 100)
	private String departmentName;

	@Column(nullable = false, length = 100)
	private String scheduleText;

	@Column(length = 100)
	private String classroom;

	@Column(nullable = false)
	private int semesterYear;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private SemesterTerm semesterTerm;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private CourseSourceType sourceType;

	@Builder
	private Course(
		String courseCode,
		String courseName,
		String professorName,
		String departmentName,
		String scheduleText,
		String classroom,
		int semesterYear,
		SemesterTerm semesterTerm,
		CourseSourceType sourceType
	) {
		this.courseCode = courseCode;
		this.courseName = courseName;
		this.professorName = professorName;
		this.departmentName = departmentName;
		this.scheduleText = scheduleText;
		this.classroom = classroom;
		this.semesterYear = semesterYear;
		this.semesterTerm = semesterTerm;
		this.sourceType = sourceType;
	}

	public void updateFromImport(
		String courseName,
		String professorName,
		String departmentName,
		String scheduleText,
		String classroom,
		int semesterYear,
		SemesterTerm semesterTerm,
		CourseSourceType sourceType
	) {
		this.courseName = courseName;
		this.professorName = professorName;
		this.departmentName = departmentName;
		this.scheduleText = scheduleText;
		this.classroom = classroom;
		this.semesterYear = semesterYear;
		this.semesterTerm = semesterTerm;
		this.sourceType = sourceType;
	}
}
