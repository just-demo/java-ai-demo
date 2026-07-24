package just.demo.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Customizes the generated OpenAPI document title/description shown in Swagger UI.
 */
@Configuration
public class OpenApiConfig {

	@Bean
	OpenAPI ragDemoOpenApi() {
		return new OpenAPI().info(new Info()
				.title("java-ai-demo RAG API")
				.version("v1")
				.description("Spring AI RAG demo backed by OpenAI (gpt-5-mini / text-embedding-3-small) "
						+ "and Elasticsearch as the vector store."));
	}
}
