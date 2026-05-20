package com.devglan.service;

import com.devglan.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@RequiredArgsConstructor
@Service
public class SearchService {

    private final ChatClient chatClient;

    public Flux<RagChunkResponse> searchStream(
            ChatRequest chatRequest) {

        AtomicReference<List<Citation>> citationsRef =
                new AtomicReference<>(Collections.emptyList());

        Flux<RagChunkResponse> contentStream = chatClient.prompt()

                .user(chatRequest.getQuery())

                .advisors(a -> a.param(
                        ChatMemory.CONVERSATION_ID,
                        chatRequest.getConversationId()
                ))

                .stream()

                .chatClientResponse()

                .map(response -> {

                    // store citations for final event
                    List<Citation> citations =
                            (List<Citation>) response.context()
                                    .getOrDefault(
                                            "citations",
                                            Collections.emptyList()
                                    );

                    if (!citations.isEmpty()) {
                        citationsRef.set(citations);
                    }

                    String content = response.chatResponse()
                            .getResult()
                            .getOutput()
                            .getText();

                    return RagChunkResponse.builder()
                            .type("CONTENT")
                            .content(content)
                            .build();
                });

        // ---------------------------------------
        // FINAL CITATIONS EVENT
        // ---------------------------------------

        Flux<RagChunkResponse> citationStream =
                Flux.defer(() -> {

                    List<Citation> citations =
                            citationsRef.get();

                    if (citations.isEmpty()) {
                        return Flux.empty();
                    }

                    return Flux.just(
                            RagChunkResponse.builder()
                                    .type("CITATIONS")
                                    .citations(citations)
                                    .build()
                    );
                });

        return contentStream.concatWith(citationStream);
    }

    @SneakyThrows
    public RagResponse search(ChatRequest request) {

        ChatClientResponse response = chatClient.prompt()
                .user(request.getQuery())
                .advisors(a -> a.param(
                        ChatMemory.CONVERSATION_ID,
                        request.getConversationId()
                ))
                .call()
                .chatClientResponse();

        String answer = response.chatResponse()
                .getResult()
                .getOutput()
                .getText();

        List<Citation> citations =
                (List<Citation>) response.context()
                        .getOrDefault(
                                "citations",
                                Collections.emptyList()
                        );

        RecipeSearchRequest searchRequest =
                (RecipeSearchRequest)
                        response.context()
                                .get("searchRequest");

        if (searchRequest != null) {

            log.info("""
                    Original Query: {}
                    Rewritten Query: {}
                    Citations Count: {}
                    """,
                    searchRequest.getOriginalQuery(),
                    searchRequest.getRewrittenQuery(),
                    citations.size()
            );
        }

        return new RagResponse(answer, citations);
    }

}
