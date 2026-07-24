package just.demo.dto;

import java.util.List;

/**
 * Response body for POST /chat: the generated answer plus the distinct source
 * filenames of the documents the RAG pipeline used as context.
 */
public record ChatResponse(String answer, List<String> documentsUsed) {
}
