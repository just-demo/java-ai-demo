package just.demo.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {

    // Spring does not auto-create any default ChatClient bean as it is meant to be customized once at build time -
    // default system prompt, default advisors (like our QuestionAnswerAdvisor), default tools/options.
    @Bean
    ChatClient chatClient(ChatClient.Builder builder) {
        return builder.build();
    }

    // ChatMemory itself comes from Spring AI's auto-configuration (in-memory by default). The advisor is
    // stateless per request - the conversation id flows through the per-call advisor context - so it's
    // safe to build once here rather than per request.
    @Bean
    MessageChatMemoryAdvisor messageChatMemoryAdvisor(ChatMemory chatMemory) {
        return MessageChatMemoryAdvisor.builder(chatMemory).build();
    }
}
