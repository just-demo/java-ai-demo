package just.demo.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Builds the singleton ChatClient used by RagService from the auto-configured
 * ChatClient.Builder (which is wired to the OpenAI ChatModel via application.properties).
 */
@Configuration
public class ChatClientConfig {

	@Bean
	ChatClient chatClient(ChatClient.Builder builder) {
		return builder.build();
	}
}
