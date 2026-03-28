package com.khunect.backend.common.security;

import com.khunect.backend.common.config.JwtProperties;
import com.khunect.backend.common.exception.CustomException;
import com.khunect.backend.common.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

	private static final String CLAIM_USER_ID = "uid";
	private static final String CLAIM_ROLE = "role";
	private static final String CLAIM_TOKEN_TYPE = "tokenType";
	private static final String ACCESS_TOKEN_TYPE = "access";
	private static final String REFRESH_TOKEN_TYPE = "refresh";

	private final JwtProperties jwtProperties;
	private final SecretKey secretKey;

	public JwtTokenProvider(JwtProperties jwtProperties) {
		this.jwtProperties = jwtProperties;
		this.secretKey = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
	}

	public String createAccessToken(Long userId, String email, String role) {
		Instant now = Instant.now();
		return createToken(
			userId,
			email,
			role,
			ACCESS_TOKEN_TYPE,
			now.plusSeconds(jwtProperties.getAccessTokenExpirationSeconds())
		);
	}

	public String createAccessToken(String email) {
		return createAccessToken(0L, email, "USER");
	}

	public String createRefreshToken(Long userId, String email, String role) {
		Instant now = Instant.now();
		return createToken(
			userId,
			email,
			role,
			REFRESH_TOKEN_TYPE,
			now.plusSeconds(jwtProperties.getRefreshTokenExpirationSeconds())
		);
	}

	public String getSubject(String token) {
		try {
			Claims claims = parseClaims(token);
			validateTokenType(claims, ACCESS_TOKEN_TYPE);
			return claims.getSubject();
		} catch (JwtException | IllegalArgumentException exception) {
			throw new CustomException(ErrorCode.INVALID_JWT_TOKEN);
		}
	}

	public Authentication getAuthentication(String token) {
		Claims claims = getAccessTokenClaims(token);
		String role = claims.get(CLAIM_ROLE, String.class);
		return UsernamePasswordAuthenticationToken.authenticated(
			claims.getSubject(),
			token,
			List.of(new SimpleGrantedAuthority("ROLE_" + role))
		);
	}

	public boolean isValidAccessToken(String token) {
		try {
			getAccessTokenClaims(token);
			return true;
		} catch (CustomException exception) {
			return false;
		}
	}

	public Claims getAccessTokenClaims(String token) {
		try {
			Claims claims = parseClaims(token);
			validateTokenType(claims, ACCESS_TOKEN_TYPE);
			return claims;
		} catch (JwtException | IllegalArgumentException exception) {
			throw new CustomException(ErrorCode.INVALID_JWT_TOKEN);
		}
	}

	public Claims getRefreshTokenClaims(String token) {
		try {
			Claims claims = parseClaims(token);
			validateTokenType(claims, REFRESH_TOKEN_TYPE);
			return claims;
		} catch (JwtException | IllegalArgumentException exception) {
			throw new CustomException(ErrorCode.INVALID_JWT_TOKEN);
		}
	}

	public Instant getRefreshTokenExpiration(String token) {
		return getRefreshTokenClaims(token).getExpiration().toInstant();
	}

	private String createToken(Long userId, String email, String role, String tokenType, Instant expiration) {
		Instant now = Instant.now();
		return Jwts.builder()
			.id(UUID.randomUUID().toString())
			.subject(email)
			.claim(CLAIM_USER_ID, userId)
			.claim(CLAIM_ROLE, role)
			.claim(CLAIM_TOKEN_TYPE, tokenType)
			.issuer(jwtProperties.getIssuer())
			.issuedAt(Date.from(now))
			.expiration(Date.from(expiration))
			.signWith(secretKey)
			.compact();
	}

	private void validateTokenType(Claims claims, String expectedType) {
		String actualType = claims.get(CLAIM_TOKEN_TYPE, String.class);
		if (!expectedType.equals(actualType)) {
			throw new CustomException(ErrorCode.INVALID_JWT_TOKEN);
		}
	}

	private Claims parseClaims(String token) {
		return Jwts.parser()
			.verifyWith(secretKey)
			.build()
			.parseSignedClaims(token)
			.getPayload();
	}
}
