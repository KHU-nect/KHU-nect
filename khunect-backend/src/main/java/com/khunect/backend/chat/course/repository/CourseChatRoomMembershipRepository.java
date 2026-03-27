package com.khunect.backend.chat.course.repository;

import com.khunect.backend.chat.course.entity.CourseChatRoomMembership;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CourseChatRoomMembershipRepository extends JpaRepository<CourseChatRoomMembership, Long> {

	Optional<CourseChatRoomMembership> findByRoomIdAndUserId(Long roomId, Long userId);

	boolean existsByRoomIdAndUserId(Long roomId, Long userId);

	@Query("""
		select membership
		from CourseChatRoomMembership membership
		join fetch membership.room room
		join fetch room.course course
		where membership.user.id = :userId
		order by room.lastMessageTime desc nulls last, room.id desc
		""")
	List<CourseChatRoomMembership> findAllByUserIdWithRoomAndCourse(Long userId);
}
