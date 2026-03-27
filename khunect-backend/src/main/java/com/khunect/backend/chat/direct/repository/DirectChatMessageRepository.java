package com.khunect.backend.chat.direct.repository;

import com.khunect.backend.chat.direct.entity.DirectChatMessage;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface DirectChatMessageRepository extends JpaRepository<DirectChatMessage, Long> {

	@Query("""
		select message
		from DirectChatMessage message
		join fetch message.sender sender
		where message.room.id = :roomId
		order by message.id desc
		""")
	List<DirectChatMessage> findLatestByRoomId(Long roomId, Pageable pageable);

	@Query("""
		select message
		from DirectChatMessage message
		join fetch message.sender sender
		where message.room.id = :roomId
		  and message.id < :beforeMessageId
		order by message.id desc
		""")
	List<DirectChatMessage> findHistoryByRoomIdAndBeforeMessageId(Long roomId, Long beforeMessageId, Pageable pageable);

	Optional<DirectChatMessage> findFirstByRoomIdOrderByIdDesc(Long roomId);
}
