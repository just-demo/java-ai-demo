package just.demo.controller;

import just.demo.dto.ChatRequest;
import just.demo.dto.ChatResponse;
import just.demo.dto.ConversationChatRequest;
import just.demo.dto.ConversationChatResponse;
import just.demo.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/chat")
    public ChatResponse chat(@RequestBody ChatRequest request) {
        return chatService.ask(request.question());
    }

    @PostMapping("/chat/conversations")
    public ConversationChatResponse chatWithMemory(@RequestBody ConversationChatRequest request) {
        return chatService.askWithMemory(request.question(), request.conversationId());
    }
}
