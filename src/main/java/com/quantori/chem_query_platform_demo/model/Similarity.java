package com.quantori.chem_query_platform_demo.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;

@Setter
@Getter
@ToString
@AllArgsConstructor
public class Similarity {
    private float min;
    private float max;
    private Map<String, Object> parameters;
    private SimilarityMetric metric;

    @Getter
    public enum SimilarityMetric {
        tanimoto("tanimoto"),
        tversky("tversky"),
        euclid("euclid-cub"),
        none("");
        @JsonValue
        private final String value;

        SimilarityMetric(String metric) {
            this.value = metric;
        }
    }
}
