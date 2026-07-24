# Spring AI

Spring AI brings the familiar Spring programming model to AI engineering. It provides
portable abstractions for chat models, embedding models, and vector stores so that
switching providers (OpenAI, Azure OpenAI, Ollama, ...) is a configuration change rather
than a code rewrite.

## Core abstractions

* ChatModel / ChatClient - talk to large language models
* EmbeddingModel - turn text into vectors
* VectorStore - store and search embeddings (Elasticsearch, PGVector, Redis, ...)
* Advisors - reusable behaviors that wrap a ChatClient call, e.g. QuestionAnswerAdvisor
  for retrieval-augmented generation (RAG)

## ChatClient

ChatClient exposes a fluent builder-style API, for example:

```
chatClient.prompt().user(question).call().content()
```
