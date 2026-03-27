package com.khunect.backend.chat.course.repository;

import com.khunect.backend.chat.course.entity.CourseChatMessage;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CourseChatMessageRepository extends JpaRepository<CourseChatMessage, Long> {

	@Query("""
		select message
		from CourseChatMessage message
		join fetch message.sender sender
		where message.room.id = :roomId
		order by message.id desc
		""")
	List<CourseChatMessage> findLatestByRoomId(Long roomId, Pageable pageable);

	@Query("""
		select message
		from CourseChatMessage message
		join fetch message.sender sender
		where message.room.id = :roomId
		  and message.id < :beforeMessageId
		order by message.id desc
		""")
	List<CourseChatMessage> findHistoryByRoomIdAndBeforeMessageId(Long roomId, Long beforeMessageId, Pageable pageable);

	Optional<CourseChatMessage> findFirstByRoomIdOrderByIdDesc(Long roomId);
}
