package com.devglan.advisors;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.devglan.dto.RecipeSearchRequest;
import com.devglan.dto.RetrievalResult;
import com.devglan.model.RecipeVectorDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
@Component
public class ElasticsearchHybridRetriever {

    private static final int K = 60;

    private final EmbeddingModel embeddingModel;
    private final ElasticsearchClient elasticsearchClient;

    public List<RecipeVectorDocument> retrieve(RecipeSearchRequest request) {

        CompletableFuture<List<RetrievalResult>> bm25Future =
                CompletableFuture.supplyAsync(() -> {
                    try {
                        return bm25Search(request);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
        CompletableFuture<List<RetrievalResult>> vectorFuture =
                CompletableFuture.supplyAsync(() -> {
                    try {
                        return vectorSearch(request);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
        List<RetrievalResult> bm25Results = bm25Future.join();
        List<RetrievalResult> vectorResults = vectorFuture.join();
        return fuse(bm25Results, vectorResults);
    }

    private List<RetrievalResult> bm25Search(RecipeSearchRequest request) throws IOException {

        SearchResponse<RecipeVectorDocument> response =
                elasticsearchClient.search(s -> s
                                .index("indian-recipes")
                                .query(q -> q.bool(b -> {
                                    b.must(m -> m.match(mm -> mm
                                            .query(request.getRewrittenQuery())
                                            .field("content")
                                    ));
                                    applyFilters(b, request);
                                    return b;
                                }))
                                .size(50),
                        RecipeVectorDocument.class
                );

        return response.hits()
                .hits()
                .stream()
                .filter(hit -> hit.source() != null)
                .map(hit -> new RetrievalResult(
                        hit.id(),
                        hit.source(),
                        hit.score()
                ))
                .toList();
    }

    private void applyFilters(BoolQuery.Builder b, RecipeSearchRequest request) {
        if (request.getCuisine() != null) {
            b.filter(f -> f.term(t -> t
                    .field("cuisine")
                    .value(request.getCuisine() + " Recipes")
            ));
        }

        // prep time
        if (request.getMaxPrepTime() != null) {
            b.filter(f -> f.range(r -> r
                    .number(n -> n
                            .field("prepTime")
                            .lte(
                                    request.getMaxPrepTime()
                                            .doubleValue()
                            )
                    )
            ));
        }

        if (request.getIngredients() != null && !request.getIngredients().isEmpty()) {
            request.getIngredients().forEach(ingredient ->
                    b.filter(f -> f.match(t -> t
                            .field("ingredients")
                            .query(ingredient)
                    ))
            );
        }
    }

    private List<RetrievalResult> vectorSearch(RecipeSearchRequest request) throws IOException {
        float[] queryVector = embeddingModel.embed(request.getRewrittenQuery());
        List<Float> vector = new ArrayList<>(queryVector.length);
        for (float v : queryVector) {
            vector.add(v);
        }
        SearchResponse<RecipeVectorDocument> response =
                elasticsearchClient.search(s -> s
                                .index("indian-recipes")
                                .knn(k -> k
                                        .field("embedding")
                                        .queryVector(vector)
                                        .k(50)
                                        .numCandidates(100)
                                        .filter(f -> f.bool(b -> {
                                            applyFilters(b, request);
                                            return b;
                                        }))
                                )
                                .size(50),
                        RecipeVectorDocument.class
                );

        return response.hits()
                .hits()
                .stream()
                .filter(hit -> hit.source() != null)
                .map(hit -> new RetrievalResult(
                        hit.id(),
                        hit.source(),
                        hit.score()
                ))
                .toList();
    }

    private List<RecipeVectorDocument> fuse(List<RetrievalResult> bm25Results, List<RetrievalResult> vectorResults) {
        Map<String, Double> scores = new HashMap<>();
        Map<String, RecipeVectorDocument> documents = new HashMap<>();
        applyRrf(bm25Results, scores, documents);
        applyRrf(vectorResults, scores, documents
        );
        return scores.entrySet()
                .stream()
                .sorted(
                        Map.Entry.<String, Double>
                                        comparingByValue()
                                .reversed()
                )
                .limit(10)
                .map(entry ->
                        documents.get(entry.getKey())
                )
                .toList();
    }

    private void applyRrf(List<RetrievalResult> results, Map<String, Double> scores,
                          Map<String, RecipeVectorDocument> documents) {

        for (int rank = 0; rank < results.size(); rank++) {
            RetrievalResult result = results.get(rank);
            double rrfScore = 1.0 / (K + rank + 1);
            scores.merge(result.documentId(), rrfScore, Double::sum);
            documents.putIfAbsent(result.documentId(), result.chunk()
            );
        }
    }


}
