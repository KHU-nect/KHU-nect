package com.khunect.backend.match.service;

import com.khunect.backend.chat.direct.entity.DirectChatRoom;
import com.khunect.backend.chat.direct.service.DirectChatService;
import com.khunect.backend.common.exception.CustomException;
import com.khunect.backend.common.exception.ErrorCode;
import com.khunect.backend.match.dto.request.CreateMatchPostRequest;
import com.khunect.backend.match.dto.request.UpdateMatchPostRequest;
import com.khunect.backend.match.dto.response.AcceptMatchPostResponse;
import com.khunect.backend.match.dto.response.MatchPostListResponse;
import com.khunect.backend.match.dto.response.MatchPostResponse;
import com.khunect.backend.match.entity.MatchPost;
import com.khunect.backend.match.entity.MatchPostStatus;
import com.khunect.backend.match.repository.MatchPostRepository;
import com.khunect.backend.user.entity.User;
import com.khunect.backend.user.service.UserService;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MatchPostService {

	private final MatchPostRepository matchPostRepository;
	private final UserService userService;
	private final DirectChatService directChatService;

	public MatchPostService(
		MatchPostRepository matchPostRepository,
		UserService userService,
		DirectChatService directChatService
	) {
		this.matchPostRepository = matchPostRepository;
		this.userService = userService;
		this.directChatService = directChatService;
	}

	@Transactional
	public MatchPostResponse create(String email, CreateMatchPostRequest request) {
		User author = userService.getOrCreateUser(email);
		MatchPost matchPost = matchPostRepository.save(MatchPost.create(
			author,
			normalize(request.preferredTimeText()),
			normalize(request.locationText()),
			normalize(request.content()),
			normalizeNullable(request.category())
		));
		return toResponse(matchPost);
	}

	public MatchPostListResponse getAll(String status, int page, int size) {
		Page<MatchPost> result = status == null || status.isBlank()
			? matchPostRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(page, size))
			: matchPostRepository.findAllByStatusOrderByCreatedAtDesc(parseStatus(status), PageRequest.of(page, size));

		return new MatchPostListResponse(
			result.getContent().stream().map(this::toResponse).toList(),
			result.getNumber(),
			result.getSize(),
			result.getTotalElements(),
			result.getTotalPages(),
			result.hasNext()
		);
	}

	public MatchPostResponse getById(Long id) {
		return toResponse(findMatchPost(id));
	}

	@Transactional
	public MatchPostResponse update(String email, Long id, UpdateMatchPostRequest request) {
		User user = userService.getOrCreateUser(email);
		MatchPost post = findMatchPost(id);
		validateAuthor(post, user.getId());
		post.update(
			normalize(request.preferredTimeText()),
			normalize(request.locationText()),
			normalize(request.content()),
			normalizeNullable(request.category())
		);
		return toResponse(post);
	}

	@Transactional
	public void delete(String email, Long id) {
		User user = userService.getOrCreateUser(email);
		MatchPost post = findMatchPost(id);
		validateAuthor(post, user.getId());
		post.cancel();
	}

	@Transactional
	public AcceptMatchPostResponse accept(String email, Long id) {
		User accepter = userService.getOrCreateUser(email);
		MatchPost post = matchPostRepository.findByIdForUpdate(id)
			.orElseThrow(() -> new CustomException(ErrorCode.MATCH_POST_NOT_FOUND));

		if (post.getAuthor().getId().equals(accepter.getId())) {
			throw new CustomException(ErrorCode.MATCH_POST_SELF_ACCEPT_NOT_ALLOWED);
		}
		if (post.getStatus() != MatchPostStatus.OPEN) {
			throw new CustomException(ErrorCode.MATCH_POST_NOT_OPEN);
		}

		DirectChatRoom room = directChatService.getOrCreateRoom(post.getAuthor(), accepter);
		post.accept(accepter, LocalDateTime.now());

		return new AcceptMatchPostResponse(post.getId(), post.getStatus().name(), room.getId());
	}

	private MatchPost findMatchPost(Long id) {
		return matchPostRepository.findById(id)
			.orElseThrow(() -> new CustomException(ErrorCode.MATCH_POST_NOT_FOUND));
	}

	private void validateAuthor(MatchPost post, Long userId) {
		if (!post.getAuthor().getId().equals(userId)) {
			throw new CustomException(ErrorCode.MATCH_POST_FORBIDDEN);
		}
	}

	private MatchPostResponse toResponse(MatchPost post) {
		return new MatchPostResponse(
			post.getId(),
			post.getAuthor().getId(),
			post.getAuthor().getNickname(),
			post.getPreferredTimeText(),
			post.getLocationText(),
			post.getContent(),
			post.getCategory(),
			post.getStatus().name(),
			post.getAcceptedBy() == null ? null : post.getAcceptedBy().getId(),
			post.getAcceptedAt(),
			post.getCreatedAt(),
			post.getUpdatedAt()
		);
	}

	private String normalize(String value) {
		return value.trim().replaceAll("\\s{2,}", " ");
	}

	private String normalizeNullable(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		return normalize(value);
	}

	private MatchPostStatus parseStatus(String status) {
		try {
			return MatchPostStatus.valueOf(status.trim().toUpperCase());
		} catch (IllegalArgumentException exception) {
			throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "유효하지 않은 match status 입니다.");
		}
	}
}
