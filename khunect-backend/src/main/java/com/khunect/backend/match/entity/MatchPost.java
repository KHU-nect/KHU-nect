package com.khunect.backend.match.entity;

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
	name = "match_post",
	indexes = {
		@Index(name = "idx_match_post_status", columnList = "status"),
		@Index(name = "idx_match_post_author", columnList = "author_id"),
		@Index(name = "idx_match_post_accepted_by", columnList = "accepted_by_id")
	}
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MatchPost extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "author_id", nullable = false)
	private User author;

	@Column(nullable = false, length = 100)
	private String preferredTimeText;

	@Column(nullable = false, length = 100)
	private String locationText;

	@Column(nullable = false, length = 1000)
	private String content;

	@Column(length = 30)
	private String category;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private MatchPostStatus status;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "accepted_by_id")
	private User acceptedBy;

	private LocalDateTime acceptedAt;

	private MatchPost(User author, String preferredTimeText, String locationText, String content, String category) {
		this.author = author;
		this.preferredTimeText = preferredTimeText;
		this.locationText = locationText;
		this.content = content;
		this.category = category;
		this.status = MatchPostStatus.OPEN;
	}

	public static MatchPost create(
		User author,
		String preferredTimeText,
		String locationText,
		String content,
		String category
	) {
		return new MatchPost(author, preferredTimeText, locationText, content, category);
	}

	public void update(String preferredTimeText, String locationText, String content, String category) {
		this.preferredTimeText = preferredTimeText;
		this.locationText = locationText;
		this.content = content;
		this.category = category;
	}

	public void accept(User accepter, LocalDateTime acceptedAt) {
		this.status = MatchPostStatus.ACCEPTED;
		this.acceptedBy = accepter;
		this.acceptedAt = acceptedAt;
	}

	public void cancel() {
		this.status = MatchPostStatus.CANCELED;
	}
}
