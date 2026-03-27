package com.khunect.backend.chat.direct.repository;

import com.khunect.backend.chat.direct.entity.DirectChatRoom;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface DirectChatRoomRepository extends JpaRepository<DirectChatRoom, Long> {

	Optional<DirectChatRoom> findByParticipantOneIdAndParticipantTwoId(Long participantOneId, Long participantTwoId);

	@Query("""
		select room
		from DirectChatRoom room
		join fetch room.participantOne p1
		join fetch room.participantTwo p2
		where p1.id = :userId or p2.id = :userId
		order by room.lastMessageTime desc nulls last, room.id desc
		""")
	List<DirectChatRoom> findAllByUserId(Long userId);
}
