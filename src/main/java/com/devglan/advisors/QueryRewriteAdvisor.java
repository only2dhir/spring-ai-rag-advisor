package com.devglan.advisors;

import com.devglan.dto.RecipeSearchRequest;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.Map;

@RequiredArgsConstructor
@Component
public class QueryRewriteAdvisor implements CallAdvisor, StreamAdvisor {

    private final ChatClient rewriteClient;

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest chatClientRequest, CallAdvisorChain callAdvisorChain) {
        ChatClientRequest updated = getChatClientRequest(chatClientRequest);
        return callAdvisorChain.nextCall(updated);
    }

    @Override
    public Flux<ChatClientResponse> adviseStream(ChatClientRequest chatClientRequest, StreamAdvisorChain streamAdvisorChain) {
        ChatClientRequest updated = getChatClientRequest(chatClientRequest);
        return streamAdvisorChain.nextStream(updated);
    }

    private @NonNull ChatClientRequest getChatClientRequest(ChatClientRequest chatClientRequest) {
        String query = chatClientRequest.prompt().getUserMessage().getText();
        RecipeSearchRequest searchRequest = rewriteClient.prompt()
                .user(query)
                .call()
                .entity(RecipeSearchRequest.class);
        searchRequest.setOriginalQuery(query);

        ChatClientRequest updated =
                chatClientRequest.mutate()
                        .context(Map.of(
                                "searchRequest", searchRequest
                        ))
                        .build();
        return updated;
    }

    @Override
    public String getName() {
        return "rewriter_advisor";
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
