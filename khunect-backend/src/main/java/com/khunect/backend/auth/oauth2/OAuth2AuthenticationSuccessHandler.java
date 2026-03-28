package com.khunect.backend.auth.oauth2;

import com.khunect.backend.auth.config.AuthProperties;
import com.khunect.backend.auth.service.AuthService;
import com.khunect.backend.common.exception.CustomException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

	private final AuthService authService;
	private final String frontendBaseUrl;
	private final AuthProperties authProperties;

	public OAuth2AuthenticationSuccessHandler(
		AuthService authService,
		@Value("${app.frontend.base-url}") String frontendBaseUrl,
		AuthProperties authProperties
	) {
		this.authService = authService;
		this.frontendBaseUrl = frontendBaseUrl;
		this.authProperties = authProperties;
	}

	@Override
	public void onAuthenticationSuccess(
		HttpServletRequest request,
		HttpServletResponse response,
		Authentication authentication
	) throws IOException, ServletException {
		try {
			OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
			String authCode = authService.handleOAuth2LoginSuccess(
				(String) oAuth2User.getAttributes().get("email"),
				(String) oAuth2User.getAttributes().get("sub")
			);
			String redirectUrl = UriComponentsBuilder.fromUriString(frontendBaseUrl)
				.path(authProperties.getCallbackPath())
				.queryParam("code", authCode)
				.build(true)
				.toUriString();
			response.sendRedirect(redirectUrl);
		} catch (CustomException exception) {
			String redirectUrl = UriComponentsBuilder.fromUriString(frontendBaseUrl)
				.path(authProperties.getCallbackPath())
				.queryParam("error", exception.getErrorCode().getCode())
				.build(true)
				.toUriString();
			response.sendRedirect(redirectUrl);
		}
	}
}
