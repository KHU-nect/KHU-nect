package com.khunect.backend.user.service;

import com.khunect.backend.common.exception.CustomException;
import com.khunect.backend.common.exception.ErrorCode;
import com.khunect.backend.interest.entity.UserInterest;
import com.khunect.backend.interest.repository.UserInterestRepository;
import com.khunect.backend.user.dto.request.CompleteSignupRequest;
import com.khunect.backend.user.dto.request.UpdateProfileRequest;
import com.khunect.backend.user.dto.response.MyProfileResponse;
import com.khunect.backend.user.entity.User;
import com.khunect.backend.user.repository.UserRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UserService {

	private final UserRepository userRepository;
	private final UserInterestRepository userInterestRepository;

	public UserService(UserRepository userRepository, UserInterestRepository userInterestRepository) {
		this.userRepository = userRepository;
		this.userInterestRepository = userInterestRepository;
	}

	@Transactional
	public MyProfileResponse completeSignup(String email, CompleteSignupRequest request) {
		User user = getOrCreateUser(email);
		if (user.isSignupCompleted()) {
			throw new CustomException(ErrorCode.SIGNUP_ALREADY_COMPLETED);
		}
		if (userRepository.existsByStudentNumber(request.studentNumber())
			&& !user.hasStudentNumber(request.studentNumber())) {
			throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "이미 사용 중인 학번입니다.");
		}

		user.completeSignup(
			normalizeText(request.nickname()),
			normalizeText(request.major()),
			request.studentNumber().trim()
		);

		return toMyProfileResponse(user, loadInterestSummaries(user));
	}

	public MyProfileResponse getMyProfile(String email) {
		User user = getOrCreateUser(email);
		return toMyProfileResponse(user, loadInterestSummaries(user));
	}

	@Transactional
	public MyProfileResponse updateProfile(String email, UpdateProfileRequest request) {
		User user = getOrCreateUser(email);
		user.updateProfile(
			normalizeText(request.nickname()),
			normalizeText(request.major()),
			normalizeText(request.bio()),
			normalizeText(request.todayQuestion())
		);
		return toMyProfileResponse(user, loadInterestSummaries(user));
	}

	@Transactional
	public User getOrCreateUser(String email) {
		return userRepository.findByEmail(email)
			.orElseGet(() -> userRepository.save(User.create(email)));
	}

	private List<MyProfileResponse.InterestSummary> loadInterestSummaries(User user) {
		return userInterestRepository.findAllByUserIdWithInterest(user.getId()).stream()
			.map(this::toInterestSummary)
			.toList();
	}

	private MyProfileResponse.InterestSummary toInterestSummary(UserInterest userInterest) {
		return new MyProfileResponse.InterestSummary(
			userInterest.getInterest().getId(),
			userInterest.getInterest().getName()
		);
	}

	private MyProfileResponse toMyProfileResponse(
		User user,
		List<MyProfileResponse.InterestSummary> interests
	) {
		return new MyProfileResponse(
			user.getId(),
			user.getEmail(),
			user.getNickname(),
			user.getMajor(),
			user.getStudentNumber(),
			user.isSignupCompleted(),
			user.getPoint(),
			user.getLevel(),
			interests,
			user.getBio(),
			user.getTodayQuestion()
		);
	}

	private String normalizeText(String value) {
		return value == null ? null : value.trim().replaceAll("\\s{2,}", " ");
	}
}
