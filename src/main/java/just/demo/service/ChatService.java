package just.demo.service;

import just.demo.dto.ChatResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

import static org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor.RETRIEVED_DOCUMENTS;

@Service
@RequiredArgsConstructor
public class ChatService {

    public static final String FILENAME_METADATA_KEY = "filename";

    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    public ChatResponse ask(String question) {
        QuestionAnswerAdvisor qaAdvisor = QuestionAnswerAdvisor.builder(vectorStore).build();

        org.springframework.ai.chat.model.ChatResponse aiResponse = chatClient.prompt()
                .advisors(qaAdvisor)
                .user(question)
                .call()
                .chatResponse();

        String answer = aiResponse.getResult().getOutput().getText();

        List<Document> retrieved = aiResponse.getMetadata().get(RETRIEVED_DOCUMENTS);
        List<String> documentsUsed = retrieved == null ? List.of() : retrieved.stream()
                .map(Document::getMetadata)
                .map(metadata -> metadata.get(FILENAME_METADATA_KEY))
                .filter(Objects::nonNull)
                .map(String.class::cast)
                .toList();

        return new ChatResponse(answer, documentsUsed);
    }
}
