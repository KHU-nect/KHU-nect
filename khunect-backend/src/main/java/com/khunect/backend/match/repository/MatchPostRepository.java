package com.khunect.backend.match.repository;

import com.khunect.backend.match.entity.MatchPost;
import com.khunect.backend.match.entity.MatchPostStatus;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import jakarta.persistence.LockModeType;

public interface MatchPostRepository extends JpaRepository<MatchPost, Long> {

	@EntityGraph(attributePaths = {"author", "acceptedBy"})
	Page<MatchPost> findAllByStatusOrderByCreatedAtDesc(MatchPostStatus status, Pageable pageable);

	@EntityGraph(attributePaths = {"author", "acceptedBy"})
	Page<MatchPost> findAllByOrderByCreatedAtDesc(Pageable pageable);

	@EntityGraph(attributePaths = {"author", "acceptedBy"})
	Optional<MatchPost> findById(Long id);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select post from MatchPost post join fetch post.author left join fetch post.acceptedBy where post.id = :id")
	Optional<MatchPost> findByIdForUpdate(Long id);
}
