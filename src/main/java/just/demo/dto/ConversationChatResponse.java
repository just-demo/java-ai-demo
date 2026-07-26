package just.demo.dto;

import java.util.List;

public record ConversationChatResponse(String answer, List<String> documentsUsed, String conversationId) {
}
