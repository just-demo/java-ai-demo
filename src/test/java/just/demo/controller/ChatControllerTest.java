package just.demo.controller;

import just.demo.dto.ChatResponse;
import just.demo.service.RagService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChatController.class)
class ChatControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private RagService ragService;

	@Test
	void returnsAnswerAndDocumentsUsed() throws Exception {
		given(ragService.ask("What is Spring Boot?"))
				.willReturn(new ChatResponse("Spring Boot is a framework...", List.of("spring-boot.md")));

		mockMvc.perform(post("/chat")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"question\":\"What is Spring Boot?\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.answer").value("Spring Boot is a framework..."))
				.andExpect(jsonPath("$.documentsUsed[0]").value("spring-boot.md"));
	}
}
