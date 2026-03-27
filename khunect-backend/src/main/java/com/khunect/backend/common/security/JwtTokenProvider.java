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
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

	private final JwtProperties jwtProperties;
	private final SecretKey secretKey;

	public JwtTokenProvider(JwtProperties jwtProperties) {
		this.jwtProperties = jwtProperties;
		this.secretKey = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
	}

	public String createAccessToken(String subject) {
		Instant now = Instant.now();
		return Jwts.builder()
			.subject(subject)
			.issuer(jwtProperties.getIssuer())
			.issuedAt(Date.from(now))
			.expiration(Date.from(now.plusSeconds(jwtProperties.getAccessTokenExpirationSeconds())))
			.signWith(secretKey)
			.compact();
	}

	public String getSubject(String token) {
		try {
			return parseClaims(token).getSubject();
		} catch (JwtException | IllegalArgumentException exception) {
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
