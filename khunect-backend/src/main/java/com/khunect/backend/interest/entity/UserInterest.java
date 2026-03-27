package com.khunect.backend.interest.entity;

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
	name = "user_interest",
	uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "interest_id"})
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserInterest extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "interest_id", nullable = false)
	private Interest interest;

	private UserInterest(User user, Interest interest) {
		this.user = user;
		this.interest = interest;
	}

	public static UserInterest create(User user, Interest interest) {
		return new UserInterest(user, interest);
	}
}
