package com.khunect.backend.auth.oauth2;

import com.khunect.backend.auth.config.AuthProperties;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class OAuth2AuthenticationFailureHandler implements AuthenticationFailureHandler {

	private final String frontendBaseUrl;
	private final AuthProperties authProperties;

	public OAuth2AuthenticationFailureHandler(
		@Value("${app.frontend.base-url}") String frontendBaseUrl,
		AuthProperties authProperties
	) {
		this.frontendBaseUrl = frontendBaseUrl;
		this.authProperties = authProperties;
	}

	@Override
	public void onAuthenticationFailure(
		HttpServletRequest request,
		HttpServletResponse response,
		AuthenticationException exception
	) throws IOException, ServletException {
		String errorCode = exception instanceof OAuth2AuthenticationException oauth2AuthenticationException
			? oauth2AuthenticationException.getError().getErrorCode()
			: "AUTH-401";
		String redirectUrl = UriComponentsBuilder.fromUriString(frontendBaseUrl)
			.path(authProperties.getCallbackPath())
			.queryParam("error", errorCode)
			.build(true)
			.toUriString();
		response.sendRedirect(redirectUrl);
	}
}
