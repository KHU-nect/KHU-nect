package com.khunect.backend.chat.direct.entity;

import com.khunect.backend.common.entity.BaseTimeEntity;
import com.khunect.backend.user.entity.User;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
	name = "direct_chat_room",
	uniqueConstraints = @UniqueConstraint(columnNames = {"participant_one_id", "participant_two_id"}),
	indexes = {
		@Index(name = "idx_direct_chat_room_p1", columnList = "participant_one_id"),
		@Index(name = "idx_direct_chat_room_p2", columnList = "participant_two_id")
	}
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DirectChatRoom extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "participant_one_id", nullable = false)
	private User participantOne;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "participant_two_id", nullable = false)
	private User participantTwo;

	private LocalDateTime lastMessageTime;

	private DirectChatRoom(User participantOne, User participantTwo) {
		this.participantOne = participantOne;
		this.participantTwo = participantTwo;
	}

	public static DirectChatRoom create(User firstUser, User secondUser) {
		if (firstUser.getId() <= secondUser.getId()) {
			return new DirectChatRoom(firstUser, secondUser);
		}
		return new DirectChatRoom(secondUser, firstUser);
	}

	public void updateLastMessageTime(LocalDateTime lastMessageTime) {
		this.lastMessageTime = lastMessageTime;
	}
}
