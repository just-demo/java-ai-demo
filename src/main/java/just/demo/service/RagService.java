package just.demo.service;

import just.demo.dto.ChatResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Orchestrates the RAG flow: sends the user's question through a ChatClient
 * wrapped with a QuestionAnswerAdvisor, which performs vector similarity search
 * against the Elasticsearch VectorStore and injects matching document chunks
 * into the prompt before calling the LLM. Also extracts which source documents
 * were actually used, based on the "filename" metadata set at ingestion time.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RagService {

	private static final String FILENAME_METADATA_KEY = "filename";

	private final ChatClient chatClient;
	private final VectorStore vectorStore;

	public ChatResponse ask(String question) {
		log.debug("Handling RAG question: {}", question);

		QuestionAnswerAdvisor qaAdvisor = QuestionAnswerAdvisor.builder(vectorStore).build();

		org.springframework.ai.chat.model.ChatResponse aiResponse = chatClient.prompt()
				.advisors(qaAdvisor)
				.user(question)
				.call()
				.chatResponse();

		String answer = aiResponse.getResult().getOutput().getText();

		List<Document> retrieved = aiResponse.getMetadata()
				.get(QuestionAnswerAdvisor.RETRIEVED_DOCUMENTS);

		List<String> documentsUsed = retrieved == null
				? List.of()
				: retrieved.stream()
						.map(doc -> (String) doc.getMetadata().getOrDefault(FILENAME_METADATA_KEY, "unknown"))
						.distinct()
						.toList();

		log.debug("Answered using documents: {}", documentsUsed);
		return new ChatResponse(answer, documentsUsed);
	}
}
