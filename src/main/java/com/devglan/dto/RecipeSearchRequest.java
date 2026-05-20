package com.devglan.dto;

import lombok.Data;

import java.util.List;

@Data
public class RecipeSearchRequest {

    private String originalQuery;
    private String rewrittenQuery;
    private String cuisine;
    private Integer maxPrepTime;
    private List<String> ingredients;
    private Integer maxIngredients;
}
