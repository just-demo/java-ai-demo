package just.demo;

import org.junit.jupiter.api.Test;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * VectorStore is mocked because the real ElasticsearchVectorStore bean connects to
 * Elasticsearch during initialization (to check whether the index exists) regardless
 * of the initialize-schema setting, which would otherwise require Docker/Elasticsearch
 * to be running just to load the context.
 */
@SpringBootTest(properties = {
		"spring.ai.openai.api-key=test-key",
		"app.document-indexing.enabled=false"
})
class JavaAiDemoApplicationTests {

	@MockitoBean
	private VectorStore vectorStore;

	@Test
	void contextLoads() {
	}

}
