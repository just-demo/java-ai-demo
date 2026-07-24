# Vector Databases

A vector database stores embeddings instead of (or in addition to) normal rows.

Example: the document "Spring Boot supports dependency injection." is passed through an
embedding model, producing a vector like [0.12, -0.45, 0.91, ..., 0.34]. That vector is
stored in the database. When a user asks a question, the question is converted into a
vector the same way, and the database searches for the most similar vectors, not exact
text matches.

## Popular vector databases

* pgvector (PostgreSQL extension)
* OpenSearch / Elasticsearch
* Pinecone
* Qdrant
* Milvus
* Weaviate
* Chroma (mostly for local development)

## Similarity search

Vector databases typically index embeddings with approximate nearest neighbour (ANN)
algorithms such as HNSW and support similarity metrics like cosine similarity, dot
product, or Euclidean distance. This demo uses cosine similarity over Elasticsearch's
dense_vector field type.
