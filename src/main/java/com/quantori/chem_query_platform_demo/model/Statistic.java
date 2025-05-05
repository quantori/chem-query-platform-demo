package com.quantori.chem_query_platform_demo.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class Statistic {

    private long filtered;
    private long matched;
    private boolean inProgress;
}
