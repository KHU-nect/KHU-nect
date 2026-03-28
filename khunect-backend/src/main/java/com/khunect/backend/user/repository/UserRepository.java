package com.khunect.backend.user.repository;

import com.khunect.backend.user.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

	Optional<User> findByEmail(String email);

	Optional<User> findByGoogleSub(String googleSub);

	boolean existsByStudentNumber(String studentNumber);
}
