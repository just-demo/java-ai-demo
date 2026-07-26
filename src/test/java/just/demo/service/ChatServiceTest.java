package just.demo.service;

import just.demo.dto.ConversationChatResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(properties = {
        "spring.ai.openai.api-key=test-key",
        "app.document-indexing.enabled=false",
        "spring.profiles.active=test"
})
class ChatServiceTest {

    @MockitoBean
    private VectorStore vectorStore;

    @MockitoBean
    private ChatModel chatModel;

    @Autowired
    private ChatService chatService;

    @BeforeEach
    void setUp() {
        when(chatModel.getOptions()).thenReturn(ChatOptions.builder().build());
    }

    @Test
    void askWithMemoryGeneratesConversationIdWhenNoneSupplied() {
        when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(List.of());
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponseWith("answer"));

        ConversationChatResponse response = chatService.askWithMemory("What is Spring AI?", null);

        assertThat(response.conversationId()).isNotBlank();
        assertThat(response.answer()).isEqualTo("answer");
    }

    @Test
    void askWithMemoryCarriesHistoryAcrossCallsWithSameConversationId() {
        when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(List.of());
        when(chatModel.call(any(Prompt.class)))
                .thenReturn(chatResponseWith("first answer"))
                .thenReturn(chatResponseWith("second answer"));

        ConversationChatResponse first = chatService.askWithMemory("What is Spring AI?", null);
        ConversationChatResponse second = chatService.askWithMemory("Can you elaborate?", first.conversationId());

        assertThat(second.conversationId()).isEqualTo(first.conversationId());

        var promptCaptor = org.mockito.ArgumentCaptor.forClass(Prompt.class);
        org.mockito.Mockito.verify(chatModel, org.mockito.Mockito.times(2)).call(promptCaptor.capture());
        List<Prompt> capturedPrompts = promptCaptor.getAllValues();

        assertThat(capturedPrompts.get(1).getInstructions().size())
                .isGreaterThan(capturedPrompts.get(0).getInstructions().size());
    }

    private ChatResponse chatResponseWith(String text) {
        return new ChatResponse(List.of(new Generation(new AssistantMessage(text))));
    }
}
