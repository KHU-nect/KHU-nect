package com.khunect.backend.interest.repository;

import com.khunect.backend.interest.entity.Interest;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InterestRepository extends JpaRepository<Interest, Long> {

	Optional<Interest> findByNameIgnoreCase(String name);
}
