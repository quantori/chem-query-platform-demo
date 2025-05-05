package com.quantori.chem_query_platform_demo.service;

import static com.quantori.chem_query_platform_demo.constants.TaskFlowConstants.USER;

import com.quantori.chem_query_platform_demo.configurations.ElasticSearchConfig;
import com.quantori.chem_query_platform_demo.dto.MoleculeDto;
import com.quantori.chem_query_platform_demo.model.SearchResultResponse;
import com.quantori.chem_query_platform_demo.model.SearchStructure;
import com.quantori.chem_query_platform_demo.model.Statistic;
import com.quantori.chem_query_platform_demo.indigo.IndigoMoleculeDecoder;
import com.quantori.cqp.api.indigo.IndigoProvider;
import com.quantori.cqp.api.model.Flattened;
import com.quantori.cqp.api.util.FingerPrintUtilities;
import com.quantori.cqp.core.model.ErrorType;
import com.quantori.cqp.core.model.FetchWaitMode;
import com.quantori.cqp.core.model.SearchError;
import com.quantori.cqp.core.model.SearchRequest;
import com.quantori.cqp.core.model.SearchResult;
import com.quantori.cqp.core.model.StorageRequest;
import com.quantori.cqp.core.source.CqpService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
@Slf4j
@RequiredArgsConstructor
public class MoleculeSearchService {

    private final IndigoProvider indigoProvider;
    private final IndigoMoleculeDecoder moleculeDecoder;
    private final MoleculeDataService moleculeDataService;
    private final CqpService<?, ?, Flattened.Molecule, Flattened.Molecule> moleculeService;

    @SneakyThrows
    public SearchResultResponse search(SearchStructure structure) {
        List<String> libraryIds =
                moleculeDataService.getMoleculeLibraryIdsByName(structure.getFileIds());
        SearchRequest<Flattened.Molecule, Flattened.Molecule> request =
                getStorageSearchRequest(structure, libraryIds);
        return executeSearch(structure, request);
    }

    private SearchResultResponse executeSearch(SearchStructure searchStructure, SearchRequest<Flattened.Molecule, Flattened.Molecule> request) throws InterruptedException, ExecutionException, TimeoutException {
        var searchResponse = moleculeService.search(request)
                .toCompletableFuture()
                .get(1, TimeUnit.MINUTES);

        String searchId = searchResponse.getSearchId();
        String user = request.getUser();

        Thread.sleep(3000);

        CompletionStage<SearchResultResponse> futureResult =
                moleculeService.getNextSearchResult(searchId, 100, user)
                        .thenApply(this::toSearchResultResponse);
        try {
            SearchResultResponse searchResultResponse = futureResult.toCompletableFuture().get(5, TimeUnit.MINUTES);
            log.info("Started search [searchId={}, search={}]",
                    searchResultResponse.searchId(), searchStructure);
            return searchResultResponse;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new MoleculeLibraryException("The request was interrupted :", e);
        } catch (ExecutionException | TimeoutException e) {
            throw new MoleculeLibraryException("FSearch was failed to get search result.", e);
        }
    }

    private SearchRequest<Flattened.Molecule, Flattened.Molecule> getStorageSearchRequest(
            SearchStructure search, List<String> libraryIds
    ) {
        StorageRequest.StorageRequestBuilder storageRequestBuilder = addSearchTypeSpecificParameters(search);
        storageRequestBuilder.indexIds(libraryIds);
        var searchRequestBuilder = SearchRequest.<Flattened.Molecule, Flattened.Molecule>builder()
                .requestStorageMap(Map.of(
                        ElasticSearchConfig.STORAGE_NAME,
                        storageRequestBuilder.build()
                ))
                .resultFilter(item -> true)
                .bufferSize(400)
                .fetchLimit(-1)
                .resultTransformer(item -> {
                    item.setStructure(moleculeDecoder.normalizeFromBytes(item.getStructure()));
                    return item;
                })
                .parallelism(1)
                .isCountTask(false)
                .fetchWaitMode(FetchWaitMode.WAIT_COMPLETE)
                .user(USER);
        return searchRequestBuilder.build();
    }

    private StorageRequest.StorageRequestBuilder addSearchTypeSpecificParameters(SearchStructure searchStructure) {
        return StorageRequestUtilities.createRequestBuilder(indigoProvider, searchStructure);
    }

    private SearchResultResponse toSearchResultResponse(SearchResult<Flattened.Molecule> searchResult) {
        List<SearchError> errors = Optional.ofNullable(searchResult.getErrors()).orElse(List.of())
                .stream()
                .filter(e -> e.getType() == ErrorType.GENERAL || e.getType() == ErrorType.STORAGE)
                .toList();

        if (!errors.isEmpty()) {
            throw new MoleculeLibraryException(
                    "FSearch was failed to get search result: " + errors.get(0).getMessage());
        }
        List<MoleculeDto> resultItems = Optional.ofNullable(searchResult.getResults())
                .orElse(List.of())
                .stream()
                .map(this::toMoleculeRow)
                .toList();

        var statistic = new Statistic(
                searchResult.getFoundCount(), searchResult.getMatchedByFilterCount(), !searchResult.isSearchFinished()
        );
        return new SearchResultResponse(searchResult.getSearchId(), resultItems, searchResult.getResultCount(), statistic);
    }

    private MoleculeDto toMoleculeRow(final Flattened.Molecule item) {
        return new MoleculeDto(
                item.getId(),
                FingerPrintUtilities.encodeStructure(item.getStructure()),
                item.getMolProperties(),
                item.getCustomOrder()
        );
    }
}
