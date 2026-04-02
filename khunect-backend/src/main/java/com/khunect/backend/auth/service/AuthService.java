package com.khunect.backend.auth.service;

import com.khunect.backend.auth.config.AuthProperties;
import com.khunect.backend.auth.dto.request.AuthExchangeRequest;
import com.khunect.backend.auth.dto.request.LogoutRequest;
import com.khunect.backend.auth.dto.request.RefreshTokenRequest;
import com.khunect.backend.auth.dto.response.AuthExchangeResponse;
import com.khunect.backend.auth.dto.response.AuthUserSummary;
import com.khunect.backend.auth.dto.response.MeResponse;
import com.khunect.backend.auth.dto.response.TokenRefreshResponse;
import com.khunect.backend.auth.entity.AuthCode;
import com.khunect.backend.auth.entity.RefreshToken;
import com.khunect.backend.auth.repository.AuthCodeRepository;
import com.khunect.backend.auth.repository.RefreshTokenRepository;
import com.khunect.backend.common.exception.CustomException;
import com.khunect.backend.common.exception.ErrorCode;
import com.khunect.backend.common.security.JwtTokenProvider;
import com.khunect.backend.user.entity.User;
import com.khunect.backend.user.repository.UserRepository;
import io.jsonwebtoken.Claims;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HexFormat;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AuthService {

	private static final String DEFAULT_ROLE = "USER";

	private final UserRepository userRepository;
	private final AuthCodeRepository authCodeRepository;
	private final RefreshTokenRepository refreshTokenRepository;
	private final JwtTokenProvider jwtTokenProvider;
	private final AuthProperties authProperties;

	public AuthService(
		UserRepository userRepository,
		AuthCodeRepository authCodeRepository,
		RefreshTokenRepository refreshTokenRepository,
		JwtTokenProvider jwtTokenProvider,
		AuthProperties authProperties
	) {
		this.userRepository = userRepository;
		this.authCodeRepository = authCodeRepository;
		this.refreshTokenRepository = refreshTokenRepository;
		this.jwtTokenProvider = jwtTokenProvider;
		this.authProperties = authProperties;
	}

	@Transactional
	public String handleOAuth2LoginSuccess(String email, String googleSub) {
		validateOAuthEmail(email);
		if (googleSub == null || googleSub.isBlank()) {
			throw new CustomException(ErrorCode.OAUTH2_EMAIL_NOT_FOUND, "Google 사용자 식별 정보가 없습니다.");
		}

		User user = userRepository.findByEmail(email)
			.orElseGet(() -> userRepository.save(User.createOAuthUser(email, googleSub)));
		if (user.getGoogleSub() == null || user.getGoogleSub().isBlank()) {
			user.updateGoogleSub(googleSub);
		}

		String rawCode = UUID.randomUUID().toString().replace("-", "")
			+ UUID.randomUUID().toString().replace("-", "");
		authCodeRepository.save(
			AuthCode.create(
				rawCode,
				user,
				LocalDateTime.now().plusSeconds(authProperties.getAuthCodeExpirationSeconds())
			)
		);
		return rawCode;
	}

	@Transactional
	public AuthExchangeResponse exchange(AuthExchangeRequest request) {
		AuthCode authCode = authCodeRepository.findByCode(request.code())
			.orElseThrow(() -> new CustomException(ErrorCode.AUTH_CODE_NOT_FOUND));

		if (authCode.isUsed()) {
			throw new CustomException(ErrorCode.AUTH_CODE_ALREADY_USED);
		}
		if (authCode.isExpired(LocalDateTime.now())) {
			throw new CustomException(ErrorCode.AUTH_CODE_EXPIRED);
		}

		authCode.markUsed();
		TokenBundle tokenBundle = issueTokenBundle(authCode.getUser());
		return new AuthExchangeResponse(
			tokenBundle.accessToken(),
			tokenBundle.refreshToken(),
			authCode.getUser().isSignupCompleted(),
			toUserSummary(authCode.getUser())
		);
	}

	@Transactional
	public TokenRefreshResponse refresh(RefreshTokenRequest request) {
		Claims claims;
		try {
			claims = jwtTokenProvider.getRefreshTokenClaims(request.refreshToken());
		} catch (CustomException exception) {
			throw new CustomException(ErrorCode.REFRESH_TOKEN_NOT_FOUND);
		}

		RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(hashToken(request.refreshToken()))
			.orElseThrow(() -> new CustomException(ErrorCode.REFRESH_TOKEN_NOT_FOUND));

		if (refreshToken.isRevoked()) {
			throw new CustomException(ErrorCode.REFRESH_TOKEN_REVOKED);
		}
		if (refreshToken.isExpired(LocalDateTime.now())) {
			throw new CustomException(ErrorCode.REFRESH_TOKEN_EXPIRED);
		}

		Long tokenUserId = claims.get("uid", Long.class);
		if (!refreshToken.getUser().getId().equals(tokenUserId)) {
			throw new CustomException(ErrorCode.REFRESH_TOKEN_NOT_FOUND);
		}

		refreshToken.revoke();
		TokenBundle tokenBundle = issueTokenBundle(refreshToken.getUser());
		return new TokenRefreshResponse(tokenBundle.accessToken(), tokenBundle.refreshToken());
	}

	@Transactional
	public void logout(LogoutRequest request) {
		refreshTokenRepository.findByTokenHash(hashToken(request.refreshToken()))
			.ifPresent(RefreshToken::revoke);
	}

	public MeResponse getMe(String email) {
		User user = userRepository.findByEmail(email)
			.orElseThrow(() -> new CustomException(ErrorCode.UNAUTHORIZED));
		return new MeResponse(
			user.getId(),
			user.getEmail(),
			user.getNickname(),
			user.getMajor(),
			user.getStudentNumber(),
			user.isSignupCompleted()
		);
	}

	private void validateOAuthEmail(String email) {
		if (email == null || email.isBlank()) {
			throw new CustomException(ErrorCode.OAUTH2_EMAIL_NOT_FOUND);
		}
		String domain = authProperties.getAllowedEmailDomain();
		if (!"*".equals(domain)) {
			String requiredSuffix = "@" + domain;
			if (!email.toLowerCase().endsWith(requiredSuffix.toLowerCase())) {
				throw new CustomException(ErrorCode.OAUTH2_EMAIL_DOMAIN_NOT_ALLOWED);
			}
		}
	}

	private TokenBundle issueTokenBundle(User user) {
		String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getEmail(), DEFAULT_ROLE);
		String refreshTokenValue = jwtTokenProvider.createRefreshToken(user.getId(), user.getEmail(), DEFAULT_ROLE);
		refreshTokenRepository.save(
			RefreshToken.create(
				user,
				hashToken(refreshTokenValue),
				LocalDateTime.ofInstant(jwtTokenProvider.getRefreshTokenExpiration(refreshTokenValue), ZoneId.systemDefault())
			)
		);
		return new TokenBundle(accessToken, refreshTokenValue);
	}

	private AuthUserSummary toUserSummary(User user) {
		return new AuthUserSummary(
			user.getId(),
			user.getEmail(),
			user.getNickname(),
			user.getMajor(),
			user.getStudentNumber()
		);
	}

	private String hashToken(String token) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hashedBytes = digest.digest(token.getBytes(StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(hashedBytes);
		} catch (NoSuchAlgorithmException exception) {
			throw new IllegalStateException("SHA-256 algorithm not available", exception);
		}
	}

	private record TokenBundle(String accessToken, String refreshToken) {
	}
}
