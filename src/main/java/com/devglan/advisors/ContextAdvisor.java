package com.devglan.advisors;

import com.devglan.dto.RecipeSearchRequest;
import com.devglan.model.RecipeVectorDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ContextAdvisor implements CallAdvisor, StreamAdvisor {

    private final ElasticsearchHybridRetriever elasticRetriever;

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {

        return chain.nextCall(enrichRequest(request));
    }

    @Override
    public Flux<ChatClientResponse> adviseStream(ChatClientRequest request, StreamAdvisorChain chain) {

        return chain.nextStream(enrichRequest(request));
    }

    private ChatClientRequest enrichRequest(ChatClientRequest request) {

        RecipeSearchRequest searchRequest = (RecipeSearchRequest) request.context().get("searchRequest");

        if (searchRequest == null) {
            return request;
        }

        List<RecipeVectorDocument> documents =
                elasticRetriever.retrieve(searchRequest);

        if (documents == null || documents.isEmpty()) {

            log.warn("No context documents found");

            return request;
        }

        log.info("Fetched {} docs for query {}", documents.size(), searchRequest.getOriginalQuery());

        String context = documents.stream()

                .map(RecipeVectorDocument::getContent)

                .collect(Collectors.joining(
                        "\n\n----------------------\n\n"
                ));

        return request.mutate()

                .context(Map.of(
                        "searchRequest", searchRequest,
                        "documents", documents,
                        "ragContext", context
                ))

                .build();
    }

    @Override
    public String getName() {
        return "context-advisor";
    }

    @Override
    public int getOrder() {
        return 100;
    }
}