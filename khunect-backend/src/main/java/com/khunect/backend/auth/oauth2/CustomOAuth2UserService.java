package com.khunect.backend.auth.oauth2;

import com.khunect.backend.auth.config.AuthProperties;
import com.khunect.backend.common.exception.ErrorCode;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

	private final DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
	private final AuthProperties authProperties;

	public CustomOAuth2UserService(AuthProperties authProperties) {
		this.authProperties = authProperties;
	}

	@Override
	public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
		OAuth2User oAuth2User = delegate.loadUser(userRequest);
		String email = (String) oAuth2User.getAttributes().get("email");
		if (email == null || email.isBlank()) {
			throw new OAuth2AuthenticationException(
				new OAuth2Error(ErrorCode.OAUTH2_EMAIL_NOT_FOUND.getCode()),
				ErrorCode.OAUTH2_EMAIL_NOT_FOUND.getMessage()
			);
		}

		String requiredSuffix = "@" + authProperties.getAllowedEmailDomain();
		if (!email.toLowerCase().endsWith(requiredSuffix.toLowerCase())) {
			throw new OAuth2AuthenticationException(
				new OAuth2Error(ErrorCode.OAUTH2_EMAIL_DOMAIN_NOT_ALLOWED.getCode()),
				ErrorCode.OAUTH2_EMAIL_DOMAIN_NOT_ALLOWED.getMessage()
			);
		}

		return new DefaultOAuth2User(oAuth2User.getAuthorities(), oAuth2User.getAttributes(), "sub");
	}
}
