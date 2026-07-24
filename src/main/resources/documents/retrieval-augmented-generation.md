# Retrieval-Augmented Generation (RAG)

RAG combines a retrieval step (vector search) with a generation step (an LLM call) so the
model can answer questions using knowledge that was not in its training data, without
fine-tuning.

## The flow

1. Question -> Embedding
2. Embedding -> Vector DB similarity search
3. Vector DB -> Top K similar document chunks
4. Chunks + Question -> LLM prompt
5. LLM -> Answer

## Why RAG

* Keeps answers grounded in your own documents
* Reduces hallucination by supplying real context
* Cheaper than fine-tuning and easy to keep current - just re-index documents

## As a Java developer

With Spring AI this is typically a few lines of code: create embeddings, store them in a
VectorStore, then use a QuestionAnswerAdvisor together with ChatClient to automatically
retrieve relevant documents and inject them into the prompt sent to the LLM.
