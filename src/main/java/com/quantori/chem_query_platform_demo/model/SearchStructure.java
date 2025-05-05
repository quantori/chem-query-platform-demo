package com.quantori.chem_query_platform_demo.model;


import com.quantori.cqp.core.model.SearchType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Setter
@Getter
@ToString
public class SearchStructure {

    private List<String> fileIds;
    private String queryStructure;
    private Similarity similarity;
    private SearchType type;
}
