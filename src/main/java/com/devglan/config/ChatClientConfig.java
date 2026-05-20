package com.devglan.config;

import com.devglan.advisors.CitationAdvisor;
import com.devglan.advisors.ContextAdvisor;
import com.devglan.advisors.QueryRewriteAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {

    private static final String QUERY_REWRITE_WITH_FILTER_SYSTEM_PROMPT = """
            You are a query rewriting engine for a recipe RAG system.
            
              Extract structured retrieval filters from the user query.
    
              Return ONLY raw JSON.
    
              Rules:
              - query = optimized semantic search query
              - cuisine = cuisine type if present
              - maxPrepTime = minutes if mentioned
              - ingredients = ingredient list if mentioned
              - maxIngredients = ingredient count limit if mentioned
    
              Use null when unavailable.
            
              Example:
            
              {
                "rewrittenQuery": "spicy north indian paneer curry",
                "cuisine": "North Indian",
                "maxPrepTime": 30,
                "ingredients": ["paneer"],
                "maxIngredients": null
              }
            """;

    private static final String RECIPE_SYSTEM_PROMPT = """
            You are a helpful recipe assistant.
            
            Use ONLY the provided context.
    
            If answer is unavailable,
            say you don't know in a funny way.
            
            Context:
            
            {ragContext}
            
            Format recipe responses using markdown.
            
            Use:
            - headings
            - bullet lists
            - numbered cooking steps
            
            Keep formatting clean and readable.
            """;

    @Bean
    public ChatClient rewriteClient(ChatModel chatModel) {
        return ChatClient.builder(chatModel)
                .defaultOptions(
                        OllamaChatOptions.builder()
                                .temperature(0.0)
                )
                .defaultSystem(QUERY_REWRITE_WITH_FILTER_SYSTEM_PROMPT)
                .build();
    }

    @Bean
    public ChatMemory chatMemory() {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(new InMemoryChatMemoryRepository())
                .maxMessages(20)
                .build();
    }

    @Bean
    public MessageChatMemoryAdvisor memoryAdvisor(ChatMemory chatMemory) {
        return MessageChatMemoryAdvisor.builder(chatMemory)
                .order(50)
                .build();
    }

    @Bean
    public ChatClient chatClient(
            ChatModel chatModel,
            QueryRewriteAdvisor rewriteAdvisor,
            MessageChatMemoryAdvisor memoryAdvisor,
            ContextAdvisor contextAdvisor,
            CitationAdvisor citationAdvisor) {

        return ChatClient.builder(chatModel)
                .defaultSystem(RECIPE_SYSTEM_PROMPT)
                .defaultAdvisors(
                        rewriteAdvisor,
                        memoryAdvisor,
                        contextAdvisor,
                        citationAdvisor
                )
                .build();
    }

}
