package com.khunect.backend.interest.repository;

import com.khunect.backend.interest.entity.UserInterest;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserInterestRepository extends JpaRepository<UserInterest, Long> {

	@Query("""
		select ui
		from UserInterest ui
		join fetch ui.interest
		where ui.user.id = :userId
		order by ui.interest.name asc
		""")
	List<UserInterest> findAllByUserIdWithInterest(Long userId);

	boolean existsByUserIdAndInterestId(Long userId, Long interestId);

	Optional<UserInterest> findByUserIdAndInterestId(Long userId, Long interestId);
}
