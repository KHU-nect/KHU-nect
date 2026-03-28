package com.khunect.backend.auth.repository;

import com.khunect.backend.auth.entity.AuthCode;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthCodeRepository extends JpaRepository<AuthCode, Long> {

	@EntityGraph(attributePaths = "user")
	Optional<AuthCode> findByCode(String code);
}
