package com.quantori.chem_query_platform_demo.service;

import com.quantori.chem_query_platform_demo.configurations.ElasticSearchConfig;
import com.quantori.chem_query_platform_demo.model.SearchStructure;
import com.quantori.chem_query_platform_demo.model.Similarity;
import com.quantori.cqp.api.indigo.IndigoFingerprintCalculator;
import com.quantori.cqp.api.indigo.IndigoProvider;
import com.quantori.cqp.api.service.MoleculesFingerprintCalculator;
import com.quantori.cqp.core.model.ExactParams;
import com.quantori.cqp.core.model.SearchType;
import com.quantori.cqp.core.model.SimilarityParams;
import com.quantori.cqp.core.model.SortParams;
import com.quantori.cqp.core.model.StorageRequest;
import com.quantori.cqp.core.model.SubstructureParams;
import com.quantori.cqp.storage.elasticsearch.model.MoleculeDocument;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

@Slf4j
public class StorageRequestUtilities {
    private static final String ALPHA = "alpha";
    private static final String ALPHA_DEFAULT = "0";
    private static final String BETA = "beta";
    private static final String BETA_DEFAULT = "0";

    private StorageRequestUtilities() {
    }

    public static StorageRequest.StorageRequestBuilder createRequestBuilder(IndigoProvider indigoProvider,
                                                                            SearchStructure searchStructure) {
        var queryStructure = searchStructure.getQueryStructure();
        var similarity = searchStructure.getSimilarity();
        return StorageRequest.builder().storageName(ElasticSearchConfig.STORAGE_NAME)
            .queryFingerprint(getQueryFingerPrint(indigoProvider, searchStructure.getType(), queryStructure))
            .criteria(null)
            .sortParams(buildSortParams())
            .substructureParams(SubstructureParams.builder().searchQuery(queryStructure).build())
            .exactParams(ExactParams.builder().searchQuery(queryStructure).build())
            .similarityParams(getSimilarityParams(searchStructure, similarity)).searchType(searchStructure.getType());
    }

    private static SortParams buildSortParams() {
        SortParams.Sort sortParam = new SortParams.Sort(
                MoleculeDocument.Fields.customOrder,
                SortParams.Order.ASC,
                SortParams.Type.GENERAL
        );

        return SortParams.ofSortList(List.of(sortParam));
    }


    private static byte[] getQueryFingerPrint(IndigoProvider indigoProvider, SearchType searchType,
                                              String queryStructure) {
        if (searchType.equals(SearchType.all)) {
            return null;
        }
        MoleculesFingerprintCalculator fpCalculator = new IndigoFingerprintCalculator(indigoProvider);
        return queryStructure == null ? new byte[0] : switch (searchType) {
            case exact -> fpCalculator.exactFingerprint(queryStructure);
            case similarity -> fpCalculator.similarityFingerprint(queryStructure);
            case substructure -> fpCalculator.substructureFingerprint(queryStructure);
            default -> throw new IllegalStateException("Unexpected value: " + searchType);
        };

    }

    private static SimilarityParams getSimilarityParams(SearchStructure searchStructure, Similarity similarity) {
        if (searchStructure == null || similarity == null) {
            return null;
        }
        var similarityParams = similarity.getParameters() != null ? similarity.getParameters() : Map.of();
        return SimilarityParams.builder()
            .searchQuery(searchStructure.getQueryStructure())
            .metric(SimilarityParams.SimilarityMetric.valueOf(similarity.getMetric().name()))
            .maxSim(similarity.getMax())
            .minSim(similarity.getMin())
            .alpha((Float.parseFloat((String) similarityParams.getOrDefault(ALPHA, ALPHA_DEFAULT))))
            .beta((Float.parseFloat((String) similarityParams.getOrDefault(BETA, BETA_DEFAULT))))
            .build();
    }
}
