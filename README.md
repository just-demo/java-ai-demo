# Spring AI RAG Demo

## 1. Start Elasticsearch

```bash
docker compose up --force-recreate
docker compose ps
curl http://localhost:9200/_cluster/health?pretty
```

## 2. Configure your API key

Create `.env` file in the project root with real value of `OPENAI_API_KEY=sk-...`

## 3. Run the application

Run `just.demo.JavaAiDemoApplication` from IDE or with

```bash
./gradlew bootRun
```

On first startup the app:

* creates the `rag-documents` Elasticsearch index (schema initialization)
* ingests the sample Markdown documents from `src/main/resources/documents/` into the vector store

## 4. Verify

* Swagger UI: http://localhost:8080/swagger-ui.html
* Elasticsearch index document count: `curl http://localhost:9200/rag-documents/_count`
* Elasticsearch index documents: `curl http://localhost:9200/rag-documents/_search`

## How RAG works here

1. Your question is embedded with `text-embedding-3-small` and compared against document
   chunk embeddings stored in Elasticsearch using cosine similarity.
2. The top matching chunks are retrieved and injected into the prompt by
   `QuestionAnswerAdvisor` (a Spring AI `ChatClient` advisor).
3. `gpt-4.1-nano` generates an answer grounded in that retrieved context.
4. The filenames the retrieved chunks came from (metadata key `filename`, set at ingestion
   time in `SampleDocumentIndexer`) are returned as `documentsUsed`.
