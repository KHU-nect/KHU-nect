package com.khunect.backend.timetable.entity;

import com.khunect.backend.common.entity.BaseTimeEntity;
import com.khunect.backend.course.entity.Course;
import com.khunect.backend.user.entity.User;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
	name = "timetable_entry",
	uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "course_id"})
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TimetableEntry extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "course_id", nullable = false)
	private Course course;

	private TimetableEntry(User user, Course course) {
		this.user = user;
		this.course = course;
	}

	public static TimetableEntry create(User user, Course course) {
		return new TimetableEntry(user, course);
	}
}
