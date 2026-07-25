package just.demo.config;

import org.springframework.ai.chat.client.ChatClient;
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
}
