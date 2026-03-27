package com.khunect.backend.mypage.service;

import com.khunect.backend.mypage.dto.response.MyPageSummaryResponse;
import com.khunect.backend.timetable.repository.TimetableEntryRepository;
import com.khunect.backend.user.entity.User;
import com.khunect.backend.user.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MyPageService {

	private final UserService userService;
	private final TimetableEntryRepository timetableEntryRepository;

	public MyPageService(UserService userService, TimetableEntryRepository timetableEntryRepository) {
		this.userService = userService;
		this.timetableEntryRepository = timetableEntryRepository;
	}

	public MyPageSummaryResponse getSummary(String email) {
		User user = userService.getOrCreateUser(email);
		return new MyPageSummaryResponse(
			user.getNickname(),
			user.getMajor(),
			maskStudentNumber(user.getStudentNumber()),
			user.getPoint(),
			user.getLevel(),
			timetableEntryRepository.countByUserId(user.getId()),
			user.getSuccessfulMatchCount(),
			user.getHelpedCount(),
			new MyPageSummaryResponse.WeeklyStats(0L, 0L, 0L, 0L)
		);
	}

	private String maskStudentNumber(String studentNumber) {
		if (studentNumber == null || studentNumber.length() < 6) {
			return studentNumber;
		}
		return studentNumber.substring(0, 4) + "****" + studentNumber.substring(studentNumber.length() - 2);
	}
}
