package com.quantori.chem_query_platform_demo.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.quantori.chem_query_platform_demo.dto.MoleculeDto;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@ToString
@Getter
public record SearchResultResponse(String searchId, List<MoleculeDto> molecules, Long counter, Statistic statistic) {
    public SearchResultResponse(@JsonProperty("searchId") final String searchId,
                                @JsonProperty("molecules") final List<MoleculeDto> molecules,
                                @JsonProperty("counter") final Long counter,
                                @JsonProperty("stat") final Statistic statistic) {
        this.searchId = searchId;
        this.molecules = molecules;
        this.counter = counter;
        this.statistic = statistic;
    }
}
