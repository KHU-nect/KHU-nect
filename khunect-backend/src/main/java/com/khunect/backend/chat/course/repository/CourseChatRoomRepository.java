package com.khunect.backend.chat.course.repository;

import com.khunect.backend.chat.course.entity.CourseChatRoom;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseChatRoomRepository extends JpaRepository<CourseChatRoom, Long> {

	Optional<CourseChatRoom> findByCourseId(Long courseId);
}
