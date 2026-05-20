package com.devglan.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RagChunkResponse {

    /**
     * CONTENT | CITATIONS
     */
    private String type;

    /**
     * Streamed token/content
     */
    private String content;

    /**
     * Final citation metadata
     */
    private List<Citation> citations;
}
