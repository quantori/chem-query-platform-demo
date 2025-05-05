package com.quantori.chem_query_platform_demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public record MoleculeDto(@JsonProperty("id") String id, @JsonProperty("structure") String structure,
                          @JsonProperty("molproperties") Map<String, String> molProperties,
                          @JsonProperty("customOrder") Long customOrder) {
}
