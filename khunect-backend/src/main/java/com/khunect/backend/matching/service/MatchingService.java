package com.khunect.backend.matching.service;

import com.khunect.backend.chat.direct.entity.DirectChatRoom;
import com.khunect.backend.chat.direct.service.DirectChatService;
import com.khunect.backend.common.exception.CustomException;
import com.khunect.backend.common.exception.ErrorCode;
import com.khunect.backend.course.entity.Course;
import com.khunect.backend.course.entity.CourseSchedule;
import com.khunect.backend.course.repository.CourseScheduleRepository;
import com.khunect.backend.interest.entity.UserInterest;
import com.khunect.backend.interest.repository.UserInterestRepository;
import com.khunect.backend.matching.dto.response.AcceptMatchResponse;
import com.khunect.backend.matching.dto.response.ClassMatchResponse;
import com.khunect.backend.matching.dto.response.FreePeriodMatchResponse;
import com.khunect.backend.timetable.repository.TimetableEntryRepository;
import com.khunect.backend.user.entity.User;
import com.khunect.backend.user.repository.UserRepository;
import com.khunect.backend.user.service.UserService;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MatchingService {

	private static final LocalTime DAY_START = LocalTime.of(9, 0);
	private static final LocalTime DAY_END = LocalTime.of(19, 0);
	private static final List<String> WEEKDAYS = List.of("월", "화", "수", "목", "금");
	private static final int MIN_FREE_MINUTES = 30;

	private final UserService userService;
	private final UserRepository userRepository;
	private final TimetableEntryRepository timetableEntryRepository;
	private final CourseScheduleRepository courseScheduleRepository;
	private final UserInterestRepository userInterestRepository;
	private final DirectChatService directChatService;

	public MatchingService(
		UserService userService,
		UserRepository userRepository,
		TimetableEntryRepository timetableEntryRepository,
		CourseScheduleRepository courseScheduleRepository,
		UserInterestRepository userInterestRepository,
		DirectChatService directChatService
	) {
		this.userService = userService;
		this.userRepository = userRepository;
		this.timetableEntryRepository = timetableEntryRepository;
		this.courseScheduleRepository = courseScheduleRepository;
		this.userInterestRepository = userInterestRepository;
		this.directChatService = directChatService;
	}

	public List<FreePeriodMatchResponse> getFreePeriodMatches(String email, Long interestId) {
		User me = userService.getOrCreateUser(email);
		if (!me.isSignupCompleted()) {
			throw new CustomException(ErrorCode.SIGNUP_NOT_COMPLETED);
		}

		Map<String, List<TimeInterval>> myFree = computeFreeSlots(loadBusySlots(me.getId()));
		List<User> candidates = timetableEntryRepository.findUsersWithTimetable(me.getId(), interestId);

		List<FreePeriodMatchResponse> result = new ArrayList<>();
		for (User candidate : candidates) {
			Map<String, List<TimeInterval>> theirFree = computeFreeSlots(loadBusySlots(candidate.getId()));
			List<FreePeriodMatchResponse.FreeSlot> commonSlots = computeCommonFreeSlots(myFree, theirFree);
			if (commonSlots.isEmpty()) {
				continue;
			}
			result.add(new FreePeriodMatchResponse(
				candidate.getId(),
				candidate.getNickname(),
				candidate.getMajor(),
				toInterestSummaries(candidate.getId()),
				candidate.getBio(),
				candidate.getTodayQuestion(),
				commonSlots
			));
		}
		return result;
	}

	public List<ClassMatchResponse> getClassMatches(String email, Long interestId) {
		User me = userService.getOrCreateUser(email);
		if (!me.isSignupCompleted()) {
			throw new CustomException(ErrorCode.SIGNUP_NOT_COMPLETED);
		}

		List<User> candidates = timetableEntryRepository.findUsersWithSameCourse(me.getId(), interestId);

		List<ClassMatchResponse> result = new ArrayList<>();
		for (User candidate : candidates) {
			List<Course> commonCourses = timetableEntryRepository.findCommonCourses(me.getId(), candidate.getId());
			List<ClassMatchResponse.CommonCourse> courseDtos = commonCourses.stream()
				.map(c -> new ClassMatchResponse.CommonCourse(c.getId(), c.getCourseName(), c.getProfessorName()))
				.toList();
			result.add(new ClassMatchResponse(
				candidate.getId(),
				candidate.getNickname(),
				candidate.getMajor(),
				toClassInterestSummaries(candidate.getId()),
				candidate.getBio(),
				candidate.getTodayQuestion(),
				courseDtos
			));
		}
		return result;
	}

	@Transactional
	public AcceptMatchResponse acceptMatch(String email, Long targetUserId) {
		User me = userService.getOrCreateUser(email);
		if (!me.isSignupCompleted()) {
			throw new CustomException(ErrorCode.SIGNUP_NOT_COMPLETED);
		}
		if (me.getId().equals(targetUserId)) {
			throw new CustomException(ErrorCode.MATCHING_SELF_NOT_ALLOWED);
		}
		User target = userRepository.findById(targetUserId)
			.orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

		DirectChatRoom room = directChatService.getOrCreateRoom(me, target);
		return new AcceptMatchResponse(room.getId());
	}

	private Map<String, List<TimeInterval>> loadBusySlots(Long userId) {
		List<CourseSchedule> schedules = courseScheduleRepository.findAllByUserId(userId);
		Map<String, List<TimeInterval>> busy = new HashMap<>();
		for (CourseSchedule s : schedules) {
			busy.computeIfAbsent(s.getDay(), k -> new ArrayList<>())
				.add(new TimeInterval(s.getStartTime(), s.getEndTime()));
		}
		return busy;
	}

	private Map<String, List<TimeInterval>> computeFreeSlots(Map<String, List<TimeInterval>> busy) {
		Map<String, List<TimeInterval>> free = new HashMap<>();
		for (String day : WEEKDAYS) {
			free.put(day, subtractBusy(busy.getOrDefault(day, List.of())));
		}
		return free;
	}

	private List<TimeInterval> subtractBusy(List<TimeInterval> busy) {
		if (busy.isEmpty()) {
			return List.of(new TimeInterval(DAY_START, DAY_END));
		}
		List<TimeInterval> sorted = busy.stream()
			.sorted(Comparator.comparing(TimeInterval::start))
			.toList();

		List<TimeInterval> free = new ArrayList<>();
		LocalTime current = DAY_START;
		for (TimeInterval b : sorted) {
			LocalTime bStart = b.start().isBefore(DAY_START) ? DAY_START : b.start();
			LocalTime bEnd = b.end().isAfter(DAY_END) ? DAY_END : b.end();
			if (current.isBefore(bStart)) {
				free.add(new TimeInterval(current, bStart));
			}
			if (bEnd.isAfter(current)) {
				current = bEnd;
			}
		}
		if (current.isBefore(DAY_END)) {
			free.add(new TimeInterval(current, DAY_END));
		}
		return free;
	}

	private List<FreePeriodMatchResponse.FreeSlot> computeCommonFreeSlots(
		Map<String, List<TimeInterval>> myFree,
		Map<String, List<TimeInterval>> theirFree
	) {
		List<FreePeriodMatchResponse.FreeSlot> common = new ArrayList<>();
		for (String day : WEEKDAYS) {
			List<TimeInterval> myDay = myFree.getOrDefault(day, List.of());
			List<TimeInterval> theirDay = theirFree.getOrDefault(day, List.of());
			for (TimeInterval overlap : intersect(myDay, theirDay)) {
				long minutes = java.time.Duration.between(overlap.start(), overlap.end()).toMinutes();
				if (minutes >= MIN_FREE_MINUTES) {
					common.add(new FreePeriodMatchResponse.FreeSlot(
						day,
						overlap.start().toString(),
						overlap.end().toString()
					));
				}
			}
		}
		return common;
	}

	private List<TimeInterval> intersect(List<TimeInterval> a, List<TimeInterval> b) {
		List<TimeInterval> result = new ArrayList<>();
		int i = 0, j = 0;
		while (i < a.size() && j < b.size()) {
			LocalTime start = a.get(i).start().isAfter(b.get(j).start()) ? a.get(i).start() : b.get(j).start();
			LocalTime end = a.get(i).end().isBefore(b.get(j).end()) ? a.get(i).end() : b.get(j).end();
			if (start.isBefore(end)) {
				result.add(new TimeInterval(start, end));
			}
			if (a.get(i).end().isBefore(b.get(j).end())) {
				i++;
			} else {
				j++;
			}
		}
		return result;
	}

	private List<FreePeriodMatchResponse.InterestSummary> toInterestSummaries(Long userId) {
		return userInterestRepository.findAllByUserIdWithInterest(userId).stream()
			.map(ui -> new FreePeriodMatchResponse.InterestSummary(
				ui.getInterest().getId(),
				ui.getInterest().getName()
			))
			.toList();
	}

	private List<ClassMatchResponse.InterestSummary> toClassInterestSummaries(Long userId) {
		return userInterestRepository.findAllByUserIdWithInterest(userId).stream()
			.map(ui -> new ClassMatchResponse.InterestSummary(
				ui.getInterest().getId(),
				ui.getInterest().getName()
			))
			.toList();
	}

	private record TimeInterval(LocalTime start, LocalTime end) {}
}
