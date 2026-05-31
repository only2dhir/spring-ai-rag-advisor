# Spring AI RAG Pipeline

A production-oriented Retrieval Augmented Generation (RAG) application built using **Spring AI**, **Elasticsearch**, and **Ollama**.

This project demonstrates how to build a modern RAG pipeline using Spring AI Advisors, hybrid retrieval, metadata filtering, Reciprocal Rank Fusion (RRF), streaming responses, citations, and chat memory.

## Architecture

```text
User Query
    ↓
QueryRewriteAdvisor
    ↓
Hybrid Retriever
    ├── BM25 Search
    └── Vector Search
            ↓
         RRF Fusion
            ↓
     ContextAdvisor
            ↓
           LLM
            ↓
    CitationAdvisor
            ↓
      Streaming SSE
```

## Features

- Query rewriting using Spring AI Advisors
- Hybrid Search (BM25 + Vector Search)
- Reciprocal Rank Fusion (RRF)
- Elasticsearch-based retrieval
- Metadata-aware filtering
- Streaming responses using SSE
- Source citations
- Chat memory integration
- Ollama local LLM support
- Advisor-driven RAG architecture

## Tech Stack

- Java 21
- Spring Boot
- Spring AI
- Elasticsearch
- Ollama
- SSE (Server-Sent Events)

## Blog Post

Detailed engineering deep dive:

👉 https://www.devglan.com/spring-ai/spring-ai-rag-pipeline

## Related Spring AI Articles

### RAG & Knowledge Assistants

- https://www.devglan.com/spring-ai/spring-ai-rag-example
- https://www.devglan.com/spring-ai/rag-with-ollama-spring-ai-chromadb
- https://www.devglan.com/spring-ai/build-ai-knowledge-assistant-spring-ai
- https://www.devglan.com/spring-ai/ai-search-engine-spring-boot-elasticsearch

### Streaming Responses

- https://www.devglan.com/spring-ai/streaming-ai-responses-sse-spring-ai-chatclient

### Chat Memory & Semantic Caching

- https://www.devglan.com/spring-ai/build-ai-chat-app-spring-ai-redis
- https://www.devglan.com/spring-ai/semantic-caching-with-redis-and-spring-boot

### Tool Calling

- https://www.devglan.com/spring-ai/spring-ai-tool-calling-example

## Key Concepts Covered

- Spring AI Advisors
- Query Rewriting
- Hybrid Retrieval
- BM25 Search
- Vector Search
- Reciprocal Rank Fusion (RRF)
- Metadata Filtering
- Retrieval Augmented Generation (RAG)
- Citations
- Streaming AI Responses
- Chat Memory

## License

This project is provided for learning and experimentation purposes.
