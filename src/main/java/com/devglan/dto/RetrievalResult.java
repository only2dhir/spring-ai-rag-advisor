package com.devglan.dto;

import com.devglan.model.RecipeVectorDocument;

public record RetrievalResult(

        String documentId,

        RecipeVectorDocument chunk,

        double score

) {
}