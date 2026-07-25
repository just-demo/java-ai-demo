package just.demo.startup;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.document-indexing", name = "enabled", havingValue = "true", matchIfMissing = true)
public class SampleDocumentIndexer implements ApplicationRunner {

    private static final String DOCUMENTS_LOCATION_PATTERN = "classpath:documents/*";
    private static final String FILENAME_METADATA_KEY = "filename";

    private final VectorStore vectorStore;
    private final ResourcePatternResolver resourcePatternResolver;

    @Override
    public void run(ApplicationArguments args) throws IOException {
        Resource[] resources = resourcePatternResolver.getResources(DOCUMENTS_LOCATION_PATTERN);
        // There are several reasons for splitting documents into chunks:
        // 1. Token limits for embedding models
        // 2. Retrieval precision
        // 3. Token limits for LLMs
        TokenTextSplitter splitter = TokenTextSplitter.builder().build();
        List<Document> chunks = new ArrayList<>();
        for (Resource resource : resources) {
            TextReader reader = new TextReader(resource);
            reader.getCustomMetadata().put(FILENAME_METADATA_KEY, resource.getFilename());
            chunks.addAll(splitter.apply(reader.get()));
        }

        vectorStore.add(chunks);
    }
}
