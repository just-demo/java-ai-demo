# Spring AI RAG Demo

## 1. Start the vector store

```bash
docker compose up --force-recreate
docker compose ps
```

## 2. Configure your API key

Create `.env` file in the project root with real value of `OPENAI_API_KEY=sk-...`

## 3. Run the application

Run `just.demo.JavaAiDemoApplication` from IDE or with

```bash
./gradlew bootRun
```

On first startup the app:

* creates the vector store schema (the Postgres `vector_store` table) via schema initialization
* ingests the sample Markdown documents from `src/main/resources/documents/` into the vector store

## 4. Verify

* Swagger UI: http://localhost:8080/swagger-ui.html
* PgVector documents: connect to `localhost:5433` (see credentials in `docker-compose.yml`) and run the query
  `select * from vector_store`

## How RAG works here

1. Your question is embedded with `text-embedding-3-small` and compared against document
   chunk embeddings stored in the PgVector vector store using cosine similarity.
2. The top matching chunks are retrieved and injected into the prompt by
   `QuestionAnswerAdvisor` (a Spring AI `ChatClient` advisor).
3. `gpt-4.1-nano` generates an answer grounded in that retrieved context.
4. The filenames the retrieved chunks came from (metadata key `filename`, set at ingestion
   time in `SampleDocumentIndexer`) are returned as `documentsUsed`.

## How conversational history works

`POST /chat/conversations` behaves like `/chat` but also remembers prior turns in the same
conversation, via Spring AI's `MessageChatMemoryAdvisor`.

* Omit `conversationId` on the first call; the server generates one and returns it in the
  response. Pass it back on subsequent calls to continue the same conversation.
* Conversation history is held in an in-memory `ChatMemory` (Spring AI's default
  `MessageWindowChatMemory`) - it's process-local and does not survive an app restart.
