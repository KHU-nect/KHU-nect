package com.khunect.backend.common.config;

import com.khunect.backend.chat.course.entity.CourseChatMessage;
import com.khunect.backend.chat.course.entity.CourseChatRoom;
import com.khunect.backend.chat.course.entity.CourseChatRoomMembership;
import com.khunect.backend.chat.course.repository.CourseChatMessageRepository;
import com.khunect.backend.chat.course.repository.CourseChatRoomMembershipRepository;
import com.khunect.backend.chat.course.repository.CourseChatRoomRepository;
import com.khunect.backend.chat.direct.entity.DirectChatMessage;
import com.khunect.backend.chat.direct.entity.DirectChatRoom;
import com.khunect.backend.chat.direct.repository.DirectChatMessageRepository;
import com.khunect.backend.chat.direct.repository.DirectChatRoomRepository;
import com.khunect.backend.course.entity.Course;
import com.khunect.backend.course.repository.CourseRepository;
import com.khunect.backend.interest.entity.Interest;
import com.khunect.backend.interest.entity.UserInterest;
import com.khunect.backend.interest.repository.InterestRepository;
import com.khunect.backend.interest.repository.UserInterestRepository;
import com.khunect.backend.match.entity.MatchPost;
import com.khunect.backend.match.repository.MatchPostRepository;
import com.khunect.backend.timetable.entity.TimetableEntry;
import com.khunect.backend.timetable.repository.TimetableEntryRepository;
import com.khunect.backend.user.entity.User;
import com.khunect.backend.user.repository.UserRepository;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile({"local", "dev"})
public class LocalDemoDataSeedConfig {

	@Bean
	public CommandLineRunner localDemoDataSeedRunner(
		UserRepository userRepository,
		CourseRepository courseRepository,
		TimetableEntryRepository timetableEntryRepository,
		InterestRepository interestRepository,
		UserInterestRepository userInterestRepository,
		MatchPostRepository matchPostRepository,
		CourseChatRoomRepository courseChatRoomRepository,
		CourseChatRoomMembershipRepository courseChatRoomMembershipRepository,
		CourseChatMessageRepository courseChatMessageRepository,
		DirectChatRoomRepository directChatRoomRepository,
		DirectChatMessageRepository directChatMessageRepository
	) {
		return args -> {
			if (userRepository.count() > 0 || courseRepository.count() < 3) {
				return;
			}

			User minseo = userRepository.save(completedUser("minseo@khu.ac.kr", "민서", "Computer Science", "2024123001"));
			User jiwon = userRepository.save(completedUser("jiwon@khu.ac.kr", "지원", "Business Administration", "2024123002"));
			User taeyang = userRepository.save(completedUser("taeyang@khu.ac.kr", "태양", "Industrial Design", "2024123003"));

			Interest backend = interestRepository.save(Interest.create("Backend"));
			Interest pottery = interestRepository.save(Interest.create("도예"));
			Interest english = interestRepository.save(Interest.create("영어회화"));

			userInterestRepository.saveAll(List.of(
				UserInterest.create(minseo, backend),
				UserInterest.create(jiwon, english),
				UserInterest.create(taeyang, pottery)
			));

			List<Course> courses = courseRepository.findAll();
			Course c1 = courses.get(0);
			Course c2 = courses.get(1);
			Course c3 = courses.get(2);

			timetableEntryRepository.saveAll(List.of(
				TimetableEntry.create(minseo, c1),
				TimetableEntry.create(minseo, c3),
				TimetableEntry.create(jiwon, c3),
				TimetableEntry.create(taeyang, c1),
				TimetableEntry.create(taeyang, c2)
			));

			matchPostRepository.saveAll(List.of(
				MatchPost.create(minseo, "화요일 저녁", "중앙도서관", "자료구조 같이 복습할 분 구해요", "study"),
				MatchPost.create(jiwon, "목요일 공강", "청운관", "세계와 시민 발표팀 찾고 있어요", "team-up"),
				MatchPost.create(taeyang, "수업 후 오후", "미술관 앞", "도자문화이야기 전시 같이 볼 분", "hobby")
			));

			CourseChatRoom courseRoom = courseChatRoomRepository.save(CourseChatRoom.create(c1));
			courseChatRoomMembershipRepository.saveAll(List.of(
				CourseChatRoomMembership.create(courseRoom, minseo),
				CourseChatRoomMembership.create(courseRoom, taeyang)
			));
			CourseChatMessage courseMessage = courseChatMessageRepository.save(
				CourseChatMessage.create(courseRoom, minseo, "도자성형1 과제 같이 준비해요.")
			);
			courseRoom.updateLastMessageTime(courseMessage.getCreatedAt());

			DirectChatRoom directRoom = directChatRoomRepository.save(DirectChatRoom.create(minseo, jiwon));
			DirectChatMessage directMessage = directChatMessageRepository.save(
				DirectChatMessage.create(directRoom, jiwon, "매칭 글 보고 연락드렸어요!")
			);
			directRoom.updateLastMessageTime(directMessage.getCreatedAt());
		};
	}

	private User completedUser(String email, String nickname, String major, String studentNumber) {
		User user = User.create(email);
		user.completeSignup(nickname, major, studentNumber);
		return user;
	}
}
