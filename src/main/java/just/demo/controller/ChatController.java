package just.demo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import just.demo.dto.ChatRequest;
import just.demo.dto.ChatResponse;
import just.demo.service.RagService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST entry point for the RAG demo: accepts a question and returns an
 * LLM-generated answer plus the filenames of the documents used as context.
 */
@Tag(name = "Chat", description = "RAG-powered chat endpoint")
@RestController
@RequiredArgsConstructor
public class ChatController {

	private final RagService ragService;

	@Operation(summary = "Ask a question, answered using retrieval-augmented generation")
	@PostMapping(value = "/chat", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ChatResponse chat(@Valid @RequestBody ChatRequest request) {
		return ragService.ask(request.question());
	}
}
