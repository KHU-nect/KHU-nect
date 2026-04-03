package com.khunect.backend.chat.course.config;

import com.khunect.backend.common.config.CorsProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

	private final CorsProperties corsProperties;
	private final CourseChatStompChannelInterceptor stompChannelInterceptor;

	public WebSocketConfig(
		CorsProperties corsProperties,
		CourseChatStompChannelInterceptor stompChannelInterceptor
	) {
		this.corsProperties = corsProperties;
		this.stompChannelInterceptor = stompChannelInterceptor;
	}

	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		String[] allowedOrigins = corsProperties.getAllowedOrigins().toArray(String[]::new);
		registry.addEndpoint("/ws-stomp")
			.setAllowedOrigins(allowedOrigins);
		registry.addEndpoint("/ws")
			.setAllowedOrigins(allowedOrigins);
	}

	@Override
	public void configureMessageBroker(MessageBrokerRegistry registry) {
		registry.setApplicationDestinationPrefixes("/pub");
		registry.enableSimpleBroker("/sub");
	}

	@Override
	public void configureClientInboundChannel(ChannelRegistration registration) {
		registration.interceptors(stompChannelInterceptor);
	}
}
