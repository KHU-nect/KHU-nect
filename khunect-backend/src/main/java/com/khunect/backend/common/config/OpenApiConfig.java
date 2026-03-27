package com.khunect.backend.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

	@Bean
	public OpenAPI khunectOpenApi() {
		return new OpenAPI().info(new Info()
			.title("Khunect Backend API")
			.description("Khunect REST API and WebSocket backend documentation")
			.version("v0.1.0")
			.contact(new Contact().name("Khunect Backend")));
	}
}
