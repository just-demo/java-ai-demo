# Spring AI RAG Demo

## 1. Start the vector store

PgVector:

```bash
COMPOSE_PROFILES=pgvector docker compose up --force-recreate
docker compose ps
```

Elasticsearch:

```bash
COMPOSE_PROFILES=elasticsearch docker compose up --force-recreate
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

* creates the vector store schema (Elasticsearch `rag-documents` index, or the Postgres `vector_store` table) via schema
  initialization
* ingests the sample Markdown documents from `src/main/resources/documents/` into the vector store

## 4. Verify

* Swagger UI: http://localhost:8080/swagger-ui.html
* Elasticsearch index document count: `GET http://localhost:9200/rag-documents/_count`
* Elasticsearch index documents: `GET http://localhost:9200/rag-documents/_search?_source_includes=*`
* PgVector documents: connect to `localhost:5433` (see credentials in `docker-compose.yml`) and run the query
  `select * from vector_store`

## Switching vector stores

`-Dspring.profiles.active=<pgvector|elasticsearch>`

## How RAG works here

1. Your question is embedded with `text-embedding-3-small` and compared against document
   chunk embeddings stored in the vector store (Elasticsearch or PgVector) using cosine
   similarity.
2. The top matching chunks are retrieved and injected into the prompt by
   `QuestionAnswerAdvisor` (a Spring AI `ChatClient` advisor).
3. `gpt-4.1-nano` generates an answer grounded in that retrieved context.
4. The filenames the retrieved chunks came from (metadata key `filename`, set at ingestion
   time in `SampleDocumentIndexer`) are returned as `documentsUsed`.
