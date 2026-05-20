package com.devglan.model;

import lombok.Data;

@Data
public class RecipeVectorDocument {

    private String id;

    private String content;

    private float[] embedding;

    private String recipeName;

    private String cuisine;

    private Integer prepTime;

    private Integer ingredientCount;

    private String ingredients;

    private String sourceUrl;
    private String imgUrl;
    private double score;
}
