package com.khunect.backend.user.entity;

import com.khunect.backend.common.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true, length = 100)
	private String email;

	@Column(unique = true, length = 100)
	private String googleSub;

	@Column(length = 20)
	private String nickname;

	@Column(length = 50)
	private String major;

	@Column(length = 300)
	private String introduction;

	@Column(unique = true, length = 10)
	private String studentNumber;

	@Column(nullable = false)
	private boolean signupCompleted;

	@Column(nullable = false)
	private int point;

	@Column(nullable = false)
	private int level;

	@Column(nullable = false)
	private int successfulMatchCount;

	@Column(nullable = false)
	private int helpedCount;

	private User(String email, String googleSub) {
		this.email = email;
		this.googleSub = googleSub;
		this.signupCompleted = false;
		this.point = 0;
		this.level = 1;
		this.successfulMatchCount = 0;
		this.helpedCount = 0;
	}

	public static User create(String email) {
		return new User(email, null);
	}

	public static User createOAuthUser(String email, String googleSub) {
		return new User(email, googleSub);
	}

	public void completeSignup(String nickname, String major, String studentNumber) {
		completeSignup(nickname, major, null, studentNumber);
	}

	public void completeSignup(String nickname, String major, String introduction, String studentNumber) {
		if (signupCompleted) {
			throw new IllegalStateException("signup already completed");
		}
		this.nickname = nickname;
		this.major = major;
		this.introduction = introduction;
		this.studentNumber = studentNumber;
		this.signupCompleted = true;
	}

	public void updateProfile(String nickname, String major) {
		updateProfile(nickname, major, this.introduction);
	}

	public void updateProfile(String nickname, String major, String introduction) {
		this.nickname = nickname;
		this.major = major;
		this.introduction = introduction;
	}

	public void updateGoogleSub(String googleSub) {
		this.googleSub = googleSub;
	}

	public boolean hasStudentNumber(String studentNumber) {
		return Objects.equals(this.studentNumber, studentNumber);
	}
}
