package com.khunect.backend.chat.direct.entity;

import com.khunect.backend.common.entity.BaseTimeEntity;
import com.khunect.backend.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
	name = "direct_chat_message",
	indexes = {
		@Index(name = "idx_direct_chat_message_room", columnList = "room_id"),
		@Index(name = "idx_direct_chat_message_sender", columnList = "sender_id")
	}
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DirectChatMessage extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "room_id", nullable = false)
	private DirectChatRoom room;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "sender_id", nullable = false)
	private User sender;

	@Column(nullable = false, length = 1000)
	private String content;

	private DirectChatMessage(DirectChatRoom room, User sender, String content) {
		this.room = room;
		this.sender = sender;
		this.content = content;
	}

	public static DirectChatMessage create(DirectChatRoom room, User sender, String content) {
		return new DirectChatMessage(room, sender, content);
	}
}
