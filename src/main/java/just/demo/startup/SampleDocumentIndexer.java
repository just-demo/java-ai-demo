package just.demo.startup;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads the bundled sample Markdown documents (src/main/resources/documents/*.md)
 * into the Elasticsearch vector store on application startup, so the RAG endpoint
 * has content to retrieve from out of the box.
 *
 * Idempotent: checks the Elasticsearch document count for the configured index
 * before ingesting, so restarting the app does not duplicate documents.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.document-indexing", name = "enabled", havingValue = "true", matchIfMissing = true)
public class SampleDocumentIndexer implements ApplicationRunner {

	private static final String DOCUMENTS_LOCATION_PATTERN = "classpath:documents/*.md";
	private static final String FILENAME_METADATA_KEY = "filename";

	private final VectorStore vectorStore;
	private final ElasticsearchClient elasticsearchClient;
	private final ResourcePatternResolver resourcePatternResolver;

	@Value("${spring.ai.vectorstore.elasticsearch.index-name}")
	private String indexName;

	@Override
	public void run(ApplicationArguments args) {
		try {
			ingest();
		}
		catch (Exception e) {
			// Ingestion calls the OpenAI embedding API; don't let a missing/invalid
			// API key or a transient failure here prevent the application from
			// starting up (health, Swagger, etc. should still be reachable).
			log.error("Sample document ingestion failed - the app will start, but /chat will have no context "
					+ "to retrieve until this succeeds on a future restart", e);
		}
	}

	private void ingest() throws IOException {
		if (alreadyIndexed()) {
			log.info("Vector store index '{}' already populated - skipping sample document ingestion", indexName);
			return;
		}

		Resource[] resources = resourcePatternResolver.getResources(DOCUMENTS_LOCATION_PATTERN);
		log.info("Indexing {} sample documents into '{}'", resources.length, indexName);

		TokenTextSplitter splitter = TokenTextSplitter.builder().build();
		List<Document> chunks = new ArrayList<>();
		for (Resource resource : resources) {
			TextReader reader = new TextReader(resource);
			reader.getCustomMetadata().put(FILENAME_METADATA_KEY, resource.getFilename());
			chunks.addAll(splitter.apply(reader.get()));
		}

		vectorStore.add(chunks);
		log.info("Indexed {} chunks from {} sample documents", chunks.size(), resources.length);
	}

	private boolean alreadyIndexed() {
		try {
			Long count = elasticsearchClient.count(c -> c.index(indexName)).count();
			return count != null && count > 0;
		}
		catch (ElasticsearchException e) {
			log.info("Index '{}' not found yet ({}); will be created and populated", indexName, e.getMessage());
			return false;
		}
		catch (IOException e) {
			throw new IllegalStateException("Failed to query Elasticsearch document count for index " + indexName, e);
		}
	}
}
