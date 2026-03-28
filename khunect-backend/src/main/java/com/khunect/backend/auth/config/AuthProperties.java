package com.khunect.backend.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.auth")
public class AuthProperties {

	private String allowedEmailDomain;
	private long authCodeExpirationSeconds;
	private String callbackPath;

	public String getAllowedEmailDomain() {
		return allowedEmailDomain;
	}

	public void setAllowedEmailDomain(String allowedEmailDomain) {
		this.allowedEmailDomain = allowedEmailDomain;
	}

	public long getAuthCodeExpirationSeconds() {
		return authCodeExpirationSeconds;
	}

	public void setAuthCodeExpirationSeconds(long authCodeExpirationSeconds) {
		this.authCodeExpirationSeconds = authCodeExpirationSeconds;
	}

	public String getCallbackPath() {
		return callbackPath;
	}

	public void setCallbackPath(String callbackPath) {
		this.callbackPath = callbackPath;
	}
}
