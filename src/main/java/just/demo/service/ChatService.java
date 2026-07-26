package just.demo.service;

import just.demo.dto.ChatResponse;
import just.demo.dto.ConversationChatResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

import static java.util.Objects.requireNonNullElse;
import static java.util.UUID.randomUUID;
import static org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor.RETRIEVED_DOCUMENTS;
import static org.springframework.ai.chat.memory.ChatMemory.CONVERSATION_ID;

@Service
@RequiredArgsConstructor
public class ChatService {

    public static final String FILENAME_METADATA_KEY = "filename";

    private final ChatClient chatClient;
    private final VectorStore vectorStore;
    private final MessageChatMemoryAdvisor memoryAdvisor;

    public ChatResponse ask(String question) {
        org.springframework.ai.chat.model.ChatResponse aiResponse = chatClient.prompt()
                .advisors(questionAnswerAdvisor())
                .user(question)
                .call()
                .chatResponse();

        String answer = aiResponse.getResult().getOutput().getText();

        return new ChatResponse(answer, extractDocumentsUsed(aiResponse));
    }

    public ConversationChatResponse askWithMemory(String question, String conversationId) {
        String resolvedConversationId = requireNonNullElse(conversationId, randomUUID().toString());

        org.springframework.ai.chat.model.ChatResponse aiResponse = chatClient.prompt()
                .advisors(questionAnswerAdvisor(), memoryAdvisor)
                .advisors(advisor -> advisor.param(CONVERSATION_ID, resolvedConversationId))
                .user(question)
                .call()
                .chatResponse();

        String answer = aiResponse.getResult().getOutput().getText();

        return new ConversationChatResponse(answer, extractDocumentsUsed(aiResponse), resolvedConversationId);
    }

    private QuestionAnswerAdvisor questionAnswerAdvisor() {
        return QuestionAnswerAdvisor.builder(vectorStore)
                .searchRequest(SearchRequest.builder()
                        // Fetching only 1 document because there are very few sample documents and all of them would
                        // always be included because of similarityThreshold defaulted to 0.0. Another option could be
                        // increasing similarityThreshold, but it would be a bit more difficult to find the right value.
                        .topK(1)
                        .build())
                .build();
    }

    private List<String> extractDocumentsUsed(org.springframework.ai.chat.model.ChatResponse aiResponse) {
        List<Document> retrieved = aiResponse.getMetadata().get(RETRIEVED_DOCUMENTS);
        return retrieved == null ? List.of() : retrieved.stream()
                .map(Document::getMetadata)
                .map(metadata -> metadata.get(FILENAME_METADATA_KEY))
                .filter(Objects::nonNull)
                .map(String.class::cast)
                .toList();
    }
}
