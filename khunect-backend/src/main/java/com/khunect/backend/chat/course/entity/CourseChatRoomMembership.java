package com.khunect.backend.chat.course.entity;

import com.khunect.backend.common.entity.BaseTimeEntity;
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
	name = "course_chat_room_membership",
	uniqueConstraints = @UniqueConstraint(columnNames = {"room_id", "user_id"})
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CourseChatRoomMembership extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "room_id", nullable = false)
	private CourseChatRoom room;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	private CourseChatRoomMembership(CourseChatRoom room, User user) {
		this.room = room;
		this.user = user;
	}

	public static CourseChatRoomMembership create(CourseChatRoom room, User user) {
		return new CourseChatRoomMembership(room, user);
	}
}
