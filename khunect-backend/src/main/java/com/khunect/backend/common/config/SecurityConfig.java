package com.khunect.backend.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.khunect.backend.auth.oauth2.CustomOAuth2UserService;
import com.khunect.backend.auth.oauth2.OAuth2AuthenticationFailureHandler;
import com.khunect.backend.auth.oauth2.OAuth2AuthenticationSuccessHandler;
import com.khunect.backend.common.exception.ErrorCode;
import com.khunect.backend.common.response.ApiResponse;
import com.khunect.backend.common.response.ErrorResponse;
import com.khunect.backend.common.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

	@Bean
	public SecurityFilterChain securityFilterChain(
		HttpSecurity http,
		ObjectMapper objectMapper,
		JwtAuthenticationFilter jwtAuthenticationFilter,
		CustomOAuth2UserService customOAuth2UserService,
		OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler,
		OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler
	) throws Exception {
		http
			.csrf(AbstractHttpConfigurer::disable)
			.cors(Customizer.withDefaults())
			.httpBasic(AbstractHttpConfigurer::disable)
			.formLogin(AbstractHttpConfigurer::disable)
			.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.authorizeHttpRequests(authorize -> authorize
				.requestMatchers(
					"/error",
					"/h2-console/**",
					"/oauth2/**",
					"/login/**",
					"/ws-stomp",
					"/ws-stomp/**",
					"/swagger-ui/**",
					"/swagger-ui.html",
					"/v3/api-docs/**",
					"/api-docs/**",
					"/api/auth/exchange",
					"/api/auth/refresh",
					"/api/courses/**"
				).permitAll()
				.requestMatchers("/api/**").authenticated()
				.anyRequest().permitAll()
			)
			.oauth2Login(oauth2 -> oauth2
				.userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
				.successHandler(oAuth2AuthenticationSuccessHandler)
				.failureHandler(oAuth2AuthenticationFailureHandler)
			)
			.exceptionHandling(exception -> exception
				.authenticationEntryPoint((request, response, authException) ->
					writeErrorResponse(response, objectMapper, ErrorCode.UNAUTHORIZED)
				)
				.accessDeniedHandler((request, response, accessDeniedException) ->
					writeErrorResponse(response, objectMapper, ErrorCode.FORBIDDEN)
				)
			)
			.headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()))
			.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	private void writeErrorResponse(
		HttpServletResponse response,
		ObjectMapper objectMapper,
		ErrorCode errorCode
	) throws IOException {
		response.setStatus(errorCode.getStatus().value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding("UTF-8");
		objectMapper.writeValue(
			response.getWriter(),
			ApiResponse.error(ErrorResponse.of(
				errorCode.getCode(),
				errorCode.getMessage(),
				LocalDateTime.now()
			))
		);
	}
}
