package com.devglan.controller;

import com.devglan.dto.ChatRequest;
import com.devglan.dto.RagChunkResponse;
import com.devglan.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/search")
public class SearchController {

    private final SearchService searchService;

    @PostMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<RagChunkResponse> search(@RequestBody ChatRequest chatRequest) {
        return searchService.searchStream(chatRequest);
    }

}
