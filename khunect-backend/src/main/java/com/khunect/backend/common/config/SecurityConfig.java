package com.khunect.backend.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.khunect.backend.common.exception.ErrorCode;
import com.khunect.backend.common.response.ApiResponse;
import com.khunect.backend.common.response.ErrorResponse;
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

@Configuration
public class SecurityConfig {

	@Bean
	public SecurityFilterChain securityFilterChain(
		HttpSecurity http,
		ObjectMapper objectMapper
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
					"/ws-stomp",
					"/ws-stomp/**",
					"/swagger-ui/**",
					"/swagger-ui.html",
					"/api-docs/**",
					"/api/auth/**",
					"/api/courses/**"
				).permitAll()
				.requestMatchers("/api/**").authenticated()
				.anyRequest().permitAll()
			)
			.exceptionHandling(exception -> exception
				.authenticationEntryPoint((request, response, authException) ->
					writeErrorResponse(response, objectMapper, ErrorCode.UNAUTHORIZED)
				)
				.accessDeniedHandler((request, response, accessDeniedException) ->
					writeErrorResponse(response, objectMapper, ErrorCode.FORBIDDEN)
				)
			)
			.headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()));

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
