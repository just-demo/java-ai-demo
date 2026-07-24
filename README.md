# java-ai-demo — Spring AI RAG Demo

A Spring Boot 4 / Spring AI 2.0 demo showing Retrieval-Augmented Generation (RAG) using
OpenAI (`gpt-5-mini` + `text-embedding-3-small`) as the LLM/embedding provider and
Elasticsearch as the vector store.

## Prerequisites

* Java 25 (matches the Gradle toolchain configured in `build.gradle.kts`)
* Docker + Docker Compose
* An OpenAI API key with access to `gpt-5-mini` and `text-embedding-3-small`

## 1. Start Elasticsearch

```bash
docker compose up -d
docker compose ps
curl http://localhost:9200/_cluster/health?pretty
```

This starts a single-node Elasticsearch instance (security disabled, for local development
only) on `http://localhost:9200`.

## 2. Configure your API key

```bash
cp .env.example .env
# edit .env and set OPENAI_API_KEY=sk-...
```

`.env` is gitignored and must never be committed. `application.properties` loads it via:

```properties
spring.config.import=optional:file:.env[.properties]
```

## 3. Run the application

```bash
./gradlew bootRun
```

On first startup the app:

* creates the `rag-documents` Elasticsearch index (schema initialization)
* ingests the sample Markdown documents from `src/main/resources/documents/` into the
  vector store (skipped on subsequent restarts if the index already has documents)

## 4. Verify

* Health: `curl http://localhost:8080/actuator/health`
* Swagger UI: http://localhost:8080/swagger-ui.html
* OpenAPI JSON: http://localhost:8080/v3/api-docs
* Elasticsearch index doc count: `curl http://localhost:9200/rag-documents/_count`

## 5. Ask a question

```bash
curl -X POST http://localhost:8080/chat \
  -H "Content-Type: application/json" \
  -d '{"question": "What is Spring Boot?"}'
```

Response:

```json
{
  "answer": "...",
  "documentsUsed": ["spring-boot.md"]
}
```

You can also try this through the Swagger UI's "Try it out" feature on `POST /chat`.

## How RAG works here

1. Your question is embedded with `text-embedding-3-small` and compared against document
   chunk embeddings stored in Elasticsearch using cosine similarity.
2. The top matching chunks are retrieved and injected into the prompt by
   `QuestionAnswerAdvisor` (a Spring AI `ChatClient` advisor).
3. `gpt-5-mini` generates an answer grounded in that retrieved context.
4. The filenames the retrieved chunks came from (metadata key `filename`, set at ingestion
   time in `SampleDocumentIndexer`) are returned as `documentsUsed`.

See `src/main/resources/documents/retrieval-augmented-generation.md` and
`vector-databases.md` for the underlying concepts, and
`docs/generate-rag-example-conversation.md` for the original design conversation this
project was built from.

## Adding more documents

Drop additional `.md` files into `src/main/resources/documents/`. Ingestion only runs when
the Elasticsearch index is empty, so to re-trigger it either:

* `docker compose down -v` (removes the Elasticsearch data volume) and restart the app, or
* delete the `rag-documents` index directly: `curl -X DELETE http://localhost:9200/rag-documents`

## How embeddings are generated

OpenAI's `text-embedding-3-small` model turns each document chunk (split via
`TokenTextSplitter`) into a 1536-dimension vector, stored in Elasticsearch's `dense_vector`
field alongside the chunk text and metadata.

## How vector search works

Elasticsearch performs an approximate nearest-neighbour search over the `dense_vector`
field using cosine similarity to find the chunks most semantically similar to the embedded
question.

## Project layout

```
src/main/java/just/demo
 ├── config          ChatClientConfig, OpenApiConfig
 ├── controller       ChatController (POST /chat)
 ├── service           RagService (RAG orchestration via ChatClient + QuestionAnswerAdvisor)
 ├── dto                ChatRequest, ChatResponse
 ├── startup          SampleDocumentIndexer (ingests sample docs on boot)
 └── JavaAiDemoApplication
src/main/resources/documents   sample Markdown documents ingested at startup
docker-compose.yml               Elasticsearch (local dev)
.env.example                       template for OPENAI_API_KEY
```

## Troubleshooting

* **401/403 from OpenAI** — check `OPENAI_API_KEY` in `.env` and that it's actually loaded
  (the app fails fast on startup if it's missing or blank).
* **Connection refused to `localhost:9200`** — check `docker compose ps` and
  `docker compose logs elasticsearch`; make sure the container is healthy before starting
  the app.
* **`documentsUsed` is empty** — either no chunks matched closely enough, or ingestion
  hasn't run yet; check `curl http://localhost:9200/rag-documents/_count`.
* **Swagger UI 404** — try `/swagger-ui/index.html` as a fallback path depending on the
  resolved springdoc version.
* **Duplicate documents after restart** — shouldn't happen; `SampleDocumentIndexer` checks
  the Elasticsearch document count before ingesting. If you suspect duplicates, verify the
  count query is targeting the same `spring.ai.vectorstore.elasticsearch.index-name`
  configured in `application.properties`.

## Running tests

```bash
./gradlew test
```

The test suite overrides `spring.ai.openai.api-key`, disables Elasticsearch schema
initialization, and disables the startup indexer, so it runs without Docker or a real
OpenAI API key.
