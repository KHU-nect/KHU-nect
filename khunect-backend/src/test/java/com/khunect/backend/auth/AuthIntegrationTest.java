package com.khunect.backend.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.khunect.backend.auth.entity.AuthCode;
import com.khunect.backend.auth.entity.RefreshToken;
import com.khunect.backend.auth.oauth2.OAuth2AuthenticationSuccessHandler;
import com.khunect.backend.auth.repository.AuthCodeRepository;
import com.khunect.backend.auth.repository.RefreshTokenRepository;
import com.khunect.backend.common.security.JwtTokenProvider;
import com.khunect.backend.user.entity.User;
import com.khunect.backend.user.repository.UserRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthIntegrationTest {

	private static final String USER_EMAIL = "student@khu.ac.kr";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private AuthCodeRepository authCodeRepository;

	@Autowired
	private RefreshTokenRepository refreshTokenRepository;

	@Autowired
	private JwtTokenProvider jwtTokenProvider;

	@Autowired
	private OAuth2AuthenticationSuccessHandler successHandler;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@BeforeEach
	void setUp() {
		jdbcTemplate.execute("delete from refresh_token");
		jdbcTemplate.execute("delete from auth_code");
		jdbcTemplate.execute("delete from direct_chat_message");
		jdbcTemplate.execute("delete from direct_chat_room");
		jdbcTemplate.execute("delete from match_post");
		jdbcTemplate.execute("delete from course_chat_message");
		jdbcTemplate.execute("delete from course_chat_room_membership");
		jdbcTemplate.execute("delete from course_chat_room");
		jdbcTemplate.execute("delete from timetable_entry");
		jdbcTemplate.execute("delete from user_interest");
		jdbcTemplate.execute("delete from interest");
		jdbcTemplate.execute("delete from users");
	}

	@Test
	void oauth2SuccessHandlerCreatesAuthCodeAndRedirects() throws Exception {
		MockHttpServletResponse response = new MockHttpServletResponse();

		successHandler.onAuthenticationSuccess(
			new MockHttpServletRequest(),
			response,
			oAuth2Authentication(USER_EMAIL, "google-sub-1")
		);

		assertThat(response.getRedirectedUrl()).startsWith("http://localhost:5173/auth/callback?code=");
		assertThat(authCodeRepository.findAll()).hasSize(1);
		assertThat(userRepository.findByEmail(USER_EMAIL)).isPresent();
	}

	@Test
	void oauth2RejectsNonKhuEmail() throws Exception {
		MockHttpServletResponse response = new MockHttpServletResponse();

		successHandler.onAuthenticationSuccess(
			new MockHttpServletRequest(),
			response,
			oAuth2Authentication("outsider@gmail.com", "google-sub-2")
		);

		assertThat(response.getRedirectedUrl()).isEqualTo("http://localhost:5173/auth/callback?error=AUTH-403-1");
		assertThat(userRepository.count()).isZero();
	}

	@Test
	void exchangeSucceeds() throws Exception {
		User user = userRepository.save(User.createOAuthUser(USER_EMAIL, "google-sub-1"));
		authCodeRepository.save(AuthCode.create("valid-auth-code", user, LocalDateTime.now().plusMinutes(5)));

		mockMvc.perform(post("/api/auth/exchange")
				.contentType("application/json")
				.content("{\"code\":\"valid-auth-code\"}"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.accessToken").isString())
			.andExpect(jsonPath("$.data.refreshToken").isString())
			.andExpect(jsonPath("$.data.signupCompleted").value(false))
			.andExpect(jsonPath("$.data.user.email").value(USER_EMAIL));

		assertThat(authCodeRepository.findByCode("valid-auth-code").orElseThrow().isUsed()).isTrue();
		assertThat(refreshTokenRepository.count()).isEqualTo(1);
	}

	@Test
	void exchangeFailsForMissingExpiredAndUsedCode() throws Exception {
		User user = userRepository.save(User.createOAuthUser(USER_EMAIL, "google-sub-1"));
		authCodeRepository.save(AuthCode.create("expired-code", user, LocalDateTime.now().minusMinutes(1)));
		AuthCode usedCode = authCodeRepository.save(
			AuthCode.create("used-code", user, LocalDateTime.now().plusMinutes(5))
		);
		usedCode.markUsed();
		authCodeRepository.save(usedCode);

		mockMvc.perform(post("/api/auth/exchange")
				.contentType("application/json")
				.content("{\"code\":\"missing\"}"))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.error.code").value("AUTH-404-1"));

		mockMvc.perform(post("/api/auth/exchange")
				.contentType("application/json")
				.content("{\"code\":\"expired-code\"}"))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.error.code").value("AUTH-401-2"));

		mockMvc.perform(post("/api/auth/exchange")
				.contentType("application/json")
				.content("{\"code\":\"used-code\"}"))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.error.code").value("AUTH-409-1"));
	}

	@Test
	void refreshSucceedsAndRotatesToken() throws Exception {
		User user = userRepository.save(User.createOAuthUser(USER_EMAIL, "google-sub-1"));
		String refreshToken = jwtTokenProvider.createRefreshToken(user.getId(), user.getEmail(), "USER");
		refreshTokenRepository.save(
			RefreshToken.create(
				user,
				sha256(refreshToken),
				LocalDateTime.ofInstant(jwtTokenProvider.getRefreshTokenExpiration(refreshToken), ZoneId.systemDefault())
			)
		);

		mockMvc.perform(post("/api/auth/refresh")
				.contentType("application/json")
				.content("""
					{
					  "refreshToken": "%s"
					}
					""".formatted(refreshToken).getBytes(StandardCharsets.UTF_8)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.accessToken").isString())
			.andExpect(jsonPath("$.data.refreshToken").isString());

		assertThat(refreshTokenRepository.findAll()).hasSize(2);
		assertThat(refreshTokenRepository.findAll().stream().filter(RefreshToken::isRevoked).count()).isEqualTo(1);
	}

	@Test
	void refreshFailsForExpiredRevokedAndInvalidToken() throws Exception {
		User user = userRepository.save(User.createOAuthUser(USER_EMAIL, "google-sub-1"));
		String expiredRefreshToken = jwtTokenProvider.createRefreshToken(user.getId(), user.getEmail(), "USER");
		refreshTokenRepository.save(
			RefreshToken.create(user, sha256(expiredRefreshToken), LocalDateTime.now().minusMinutes(1))
		);
		String revokedRefreshToken = jwtTokenProvider.createRefreshToken(user.getId(), user.getEmail(), "USER");
		RefreshToken revoked = refreshTokenRepository.save(
			RefreshToken.create(user, sha256(revokedRefreshToken), LocalDateTime.now().plusDays(1))
		);
		revoked.revoke();
		refreshTokenRepository.save(revoked);

		mockMvc.perform(post("/api/auth/refresh")
				.contentType("application/json")
				.content("{\"refreshToken\":\"invalid\"}"))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.error.code").value("AUTH-401-3"));

		mockMvc.perform(post("/api/auth/refresh")
				.contentType("application/json")
				.content("""
					{
					  "refreshToken": "%s"
					}
					""".formatted(expiredRefreshToken)))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.error.code").value("AUTH-401-4"));

		mockMvc.perform(post("/api/auth/refresh")
				.contentType("application/json")
				.content("""
					{
					  "refreshToken": "%s"
					}
					""".formatted(revokedRefreshToken)))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.error.code").value("AUTH-401-5"));
	}

	@Test
	void jwtFilterProtectsApiAndAuthMeWorks() throws Exception {
		User user = userRepository.save(User.createOAuthUser(USER_EMAIL, "google-sub-1"));
		String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getEmail(), "USER");

		mockMvc.perform(get("/api/auth/me"))
			.andExpect(status().isUnauthorized());

		mockMvc.perform(get("/api/auth/me")
				.header("Authorization", "Bearer " + accessToken))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.email").value(USER_EMAIL))
			.andExpect(jsonPath("$.data.signupCompleted").value(false));
	}

	@Test
	void logoutRevokesRefreshTokenAndRefreshFailsAfterLogout() throws Exception {
		User user = userRepository.save(User.createOAuthUser(USER_EMAIL, "google-sub-1"));
		String refreshToken = jwtTokenProvider.createRefreshToken(user.getId(), user.getEmail(), "USER");
		refreshTokenRepository.save(
			RefreshToken.create(
				user,
				sha256(refreshToken),
				LocalDateTime.ofInstant(jwtTokenProvider.getRefreshTokenExpiration(refreshToken), ZoneId.systemDefault())
			)
		);
		String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getEmail(), "USER");

		mockMvc.perform(post("/api/auth/logout")
				.header("Authorization", "Bearer " + accessToken)
				.contentType("application/json")
				.content("""
					{
					  "refreshToken": "%s"
					}
					""".formatted(refreshToken)))
			.andExpect(status().isOk());

		mockMvc.perform(post("/api/auth/refresh")
				.contentType("application/json")
				.content("""
					{
					  "refreshToken": "%s"
					}
					""".formatted(refreshToken)))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.error.code").value("AUTH-401-5"));
	}

	private OAuth2AuthenticationToken oAuth2Authentication(String email, String sub) {
		OAuth2User principal = new DefaultOAuth2User(
			List.of(new SimpleGrantedAuthority("ROLE_USER")),
			Map.of("email", email, "sub", sub),
			"email"
		);
		return new OAuth2AuthenticationToken(principal, principal.getAuthorities(), "google");
	}

	private String sha256(String token) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
		} catch (NoSuchAlgorithmException exception) {
			throw new IllegalStateException(exception);
		}
	}
}
