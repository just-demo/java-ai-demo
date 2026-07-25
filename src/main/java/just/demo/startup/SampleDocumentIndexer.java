package just.demo.startup;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static just.demo.service.ChatService.FILENAME_METADATA_KEY;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.document-indexing", name = "enabled", havingValue = "true", matchIfMissing = true)
public class SampleDocumentIndexer implements ApplicationRunner {

    private static final String DOCUMENTS_LOCATION_PATTERN = "classpath:documents/*";

    private final VectorStore vectorStore;
    private final ResourcePatternResolver resourcePatternResolver;

    @Override
    public void run(ApplicationArguments args) throws IOException {
        Resource[] resources = resourcePatternResolver.getResources(DOCUMENTS_LOCATION_PATTERN);
        // There are several reasons for splitting input files into chunks:
        // 1. Token limits for embedding models
        // 2. Retrieval precision
        // 3. Token limits for LLMs
        TokenTextSplitter splitter = TokenTextSplitter.builder().build();
        List<Document> documents = new ArrayList<>();
        for (Resource resource : resources) {
            TextReader reader = new TextReader(resource);
            reader.getCustomMetadata().put(FILENAME_METADATA_KEY, resource.getFilename());
            documents.addAll(splitter.apply(reader.get()));
        }

        // A workaround for deleting all documents
        vectorStore.delete(new FilterExpressionBuilder().isNotNull(FILENAME_METADATA_KEY).build());
        vectorStore.add(documents);
    }
}
