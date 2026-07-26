package just.demo;

import org.junit.jupiter.api.Test;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;


@SpringBootTest(properties = {
		"spring.ai.openai.api-key=test-key",
		"app.document-indexing.enabled=false"
})
@Import(TestcontainersConfiguration.class)
class JavaAiDemoApplicationTests {

	@MockitoBean
	private VectorStore vectorStore;

	@Test
	void contextLoads() {
	}

}
