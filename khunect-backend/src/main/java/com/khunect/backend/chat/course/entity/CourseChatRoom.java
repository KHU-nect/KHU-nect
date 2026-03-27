package com.khunect.backend.chat.course.entity;

import com.khunect.backend.common.entity.BaseTimeEntity;
import com.khunect.backend.course.entity.Course;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
	name = "course_chat_room",
	indexes = @Index(name = "idx_course_chat_room_course", columnList = "course_id")
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CourseChatRoom extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "course_id", nullable = false, unique = true)
	private Course course;

	private LocalDateTime lastMessageTime;

	private CourseChatRoom(Course course) {
		this.course = course;
	}

	public static CourseChatRoom create(Course course) {
		return new CourseChatRoom(course);
	}

	public void updateLastMessageTime(LocalDateTime lastMessageTime) {
		this.lastMessageTime = lastMessageTime;
	}
}
