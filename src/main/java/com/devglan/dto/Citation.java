package com.devglan.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class Citation {
    private String title;
    private String sourceUrl;
    private Double score;
}
