package com.khunect.backend;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiSmokeIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void openApiDocsAndSwaggerUiAreExposed() throws Exception {
		mockMvc.perform(get("/api-docs"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.openapi").exists())
			.andExpect(jsonPath("$.info.title").value("Khunect Backend API"));

		mockMvc.perform(get("/swagger-ui/index.html"))
			.andExpect(status().isOk());
	}

	@Test
	void keyEndpointShapesAreAvailable() throws Exception {
		mockMvc.perform(get("/api/courses"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.data.content").isArray());

		mockMvc.perform(get("/api/match-posts")
				.with(user("smoke@khu.ac.kr")))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.data.content").isArray());

		mockMvc.perform(get("/api/users/me"))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.error.code").value("AUTH-401"));
	}
}
