package com.devglan.advisors;

import com.devglan.dto.Citation;
import com.devglan.model.RecipeVectorDocument;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class CitationAdvisor implements CallAdvisor, StreamAdvisor {

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest chatClientRequest, CallAdvisorChain callAdvisorChain) {
        List<Citation> citations = extractCitations(chatClientRequest);

        ChatClientResponse response = callAdvisorChain.nextCall(chatClientRequest);
        return response.mutate()
                .context(Map.of(
                        "citations", citations
                ))
                .build();
    }

    @Override
    public Flux<ChatClientResponse> adviseStream(ChatClientRequest request, StreamAdvisorChain chain) {
        List<Citation> citations = extractCitations(request);

        return chain.nextStream(request)
                .map(response -> response.mutate()
                        .context(Map.of(
                                "citations", citations
                        ))
                        .build()
                );
    }
    private List<Citation> extractCitations(ChatClientRequest request) {
        List<RecipeVectorDocument> documents = (List<RecipeVectorDocument>) request.context()
                .get("documents");

        if (documents == null || documents.isEmpty()) {
            return Collections.emptyList();
        }

        return documents.stream()
                .map(doc -> Citation.builder()
                        .title(doc.getRecipeName())
                        .sourceUrl(doc.getSourceUrl())
                        .score(doc.getScore())
                        .build())
                .distinct()
                .toList();
    }

    @Override
    public String getName() {
        return "citation-advisor";
    }

    @Override
    public int getOrder() {
        return 200;
    }

}
