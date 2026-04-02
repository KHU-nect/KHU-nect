package com.khunect.backend.chat.course.entity;

import com.khunect.backend.common.entity.BaseTimeEntity;
import com.khunect.backend.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
	name = "course_chat_message",
	indexes = {
		@Index(name = "idx_course_chat_message_room", columnList = "room_id"),
		@Index(name = "idx_course_chat_message_sender", columnList = "sender_id")
	}
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CourseChatMessage extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "room_id", nullable = false)
	private CourseChatRoom room;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "sender_id", nullable = false)
	private User sender;

	@Column(nullable = false, length = 1000)
	private String content;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private CourseChatMessageMode mode;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private SitTogetherRequestStatus sitTogetherStatus;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "sit_together_accepted_by_user_id")
	private User sitTogetherAcceptedBy;

	private LocalDateTime sitTogetherAcceptedAt;

	@Column(name = "sit_together_direct_room_id")
	private Long sitTogetherDirectRoomId;

	private CourseChatMessage(CourseChatRoom room, User sender, String content, CourseChatMessageMode mode) {
		this.room = room;
		this.sender = sender;
		this.content = content;
		this.mode = mode;
		this.sitTogetherStatus = mode == CourseChatMessageMode.SIT_TOGETHER
			? SitTogetherRequestStatus.PENDING
			: SitTogetherRequestStatus.NOT_APPLICABLE;
	}

	public static CourseChatMessage create(CourseChatRoom room, User sender, String content) {
		return create(room, sender, content, CourseChatMessageMode.GENERAL);
	}

	public static CourseChatMessage create(CourseChatRoom room, User sender, String content, CourseChatMessageMode mode) {
		return new CourseChatMessage(room, sender, content, mode);
	}

	public void acceptSitTogether(User accepter, Long directRoomId) {
		this.sitTogetherStatus = SitTogetherRequestStatus.ACCEPTED;
		this.sitTogetherAcceptedBy = accepter;
		this.sitTogetherAcceptedAt = LocalDateTime.now();
		this.sitTogetherDirectRoomId = directRoomId;
	}
}
