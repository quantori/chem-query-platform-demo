package com.quantori.chem_query_platform_demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.quantori.chem_query_platform_demo.indigo.IndigoMoleculeDecoder;
import com.quantori.chem_query_platform_demo.model.SearchResultResponse;
import com.quantori.chem_query_platform_demo.model.SearchStructure;
import com.quantori.cqp.api.indigo.IndigoProvider;
import com.quantori.cqp.api.model.Flattened;
import com.quantori.cqp.core.model.SearchRequest;
import com.quantori.cqp.core.model.SearchResult;
import com.quantori.cqp.core.model.SearchType;
import com.quantori.cqp.core.source.CqpService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletableFuture;

class MoleculeSearchServiceTest {

    private IndigoMoleculeDecoder moleculeDecoder;
    private MoleculeDataService moleculeDataService;
    private CqpService<?, ?, Flattened.Molecule, Flattened.Molecule> moleculeService;

    private MoleculeSearchService searchService;

    @BeforeEach
    void setUp() {
        IndigoProvider indigoProvider = mock(IndigoProvider.class);
        moleculeDecoder = mock(IndigoMoleculeDecoder.class);
        moleculeDataService = mock(MoleculeDataService.class);
        moleculeService = mock(CqpService.class);
        searchService = new MoleculeSearchService(indigoProvider, moleculeDecoder, moleculeDataService, moleculeService);
    }

    @Test
    void testSearch_Success() {
        SearchStructure structure = new SearchStructure();
        structure.setType(SearchType.substructure);
        structure.setFileIds(List.of("lib1"));

        List<String> ids = List.of("lib_id");
        when(moleculeDataService.getMoleculeLibraryIdsByName(any())).thenReturn(ids);

        SearchRequest<Flattened.Molecule, Flattened.Molecule> dummyRequest = mock(SearchRequest.class);
        when(dummyRequest.getUser()).thenReturn("USER");
        Flattened.Molecule molecule = new Flattened.Molecule();
        molecule.setLibraryId("id");
        molecule.setStructure("anyNonNullString".getBytes());

        SearchResult<Flattened.Molecule> finalResult = SearchResult.<Flattened.Molecule>builder()
                .searchId("search123")
                .searchFinished(true)
                .foundCount(1)
                .matchedByFilterCount(1)
                .errors(List.of())
                .resultCount(1)
                .results(List.of(molecule))
                .build();

        var initialResponse = SearchResult.<Flattened.Molecule>builder()
                .searchId("xyz").build();

        when(moleculeService.search(any())).thenReturn(CompletableFuture.completedFuture(initialResponse));
        when(moleculeService.getNextSearchResult(any(), anyInt(), any())).thenReturn(
                CompletableFuture.completedFuture(finalResult)
        );

        when(moleculeDecoder.normalizeFromBytes(any())).thenReturn("encoded".getBytes(StandardCharsets.UTF_8));
        SearchResultResponse response = searchService.search(structure);

        assertEquals("search123", response.searchId());
        assertEquals(1, response.molecules().size());
    }

    @Test
    void testSearch_Timeout_ThrowsException() {
        SearchStructure structure = new SearchStructure();
        structure.setType(SearchType.substructure);
        structure.setFileIds(List.of("lib1"));

        when(moleculeDataService.getMoleculeLibraryIdsByName(any())).thenReturn(List.of("lib_id"));

        var initial = SearchResult.<Flattened.Molecule>builder()
                .searchId("timeout")
                .build();

        when(moleculeService.search(any())).thenReturn(CompletableFuture.completedFuture(initial));
        when(moleculeService.getNextSearchResult(any(), anyInt(), any())).thenReturn(
                CompletableFuture.failedFuture(new java.util.concurrent.TimeoutException("Timeout"))
        );

        Exception ex = assertThrows(MoleculeLibraryException.class,
                () -> searchService.search(structure));
        assertTrue(ex.getMessage().contains("FSearch was failed to get search result."));
    }
}
