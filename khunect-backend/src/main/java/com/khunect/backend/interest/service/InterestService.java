package com.khunect.backend.interest.service;

import com.khunect.backend.common.exception.CustomException;
import com.khunect.backend.common.exception.ErrorCode;
import com.khunect.backend.interest.dto.request.AddInterestRequest;
import com.khunect.backend.interest.dto.response.MyInterestResponse;
import com.khunect.backend.interest.entity.Interest;
import com.khunect.backend.interest.entity.UserInterest;
import com.khunect.backend.interest.repository.InterestRepository;
import com.khunect.backend.interest.repository.UserInterestRepository;
import com.khunect.backend.user.entity.User;
import com.khunect.backend.user.service.UserService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class InterestService {

	private final InterestRepository interestRepository;
	private final UserInterestRepository userInterestRepository;
	private final UserService userService;

	public InterestService(
		InterestRepository interestRepository,
		UserInterestRepository userInterestRepository,
		UserService userService
	) {
		this.interestRepository = interestRepository;
		this.userInterestRepository = userInterestRepository;
		this.userService = userService;
	}

	public List<MyInterestResponse> getMyInterests(String email) {
		User user = userService.getOrCreateUser(email);
		return userInterestRepository.findAllByUserIdWithInterest(user.getId()).stream()
			.map(userInterest -> new MyInterestResponse(
				userInterest.getInterest().getId(),
				userInterest.getInterest().getName()
			))
			.toList();
	}

	@Transactional
	public MyInterestResponse addInterest(String email, AddInterestRequest request) {
		User user = userService.getOrCreateUser(email);
		String normalizedName = normalizeName(request.name());
		Interest interest = interestRepository.findByNameIgnoreCase(normalizedName)
			.orElseGet(() -> interestRepository.save(Interest.create(normalizedName)));

		if (userInterestRepository.existsByUserIdAndInterestId(user.getId(), interest.getId())) {
			throw new CustomException(ErrorCode.INTEREST_ALREADY_ADDED);
		}

		userInterestRepository.save(UserInterest.create(user, interest));
		return new MyInterestResponse(interest.getId(), interest.getName());
	}

	@Transactional
	public void removeInterest(String email, Long interestId) {
		User user = userService.getOrCreateUser(email);
		if (!interestRepository.existsById(interestId)) {
			throw new CustomException(ErrorCode.INTEREST_NOT_FOUND);
		}

		UserInterest userInterest = userInterestRepository.findByUserIdAndInterestId(user.getId(), interestId)
			.orElseThrow(() -> new CustomException(ErrorCode.USER_INTEREST_NOT_FOUND));

		userInterestRepository.delete(userInterest);
	}

	private String normalizeName(String value) {
		return value.trim().replaceAll("\\s{2,}", " ");
	}
}
