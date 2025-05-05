package com.quantori.chem_query_platform_demo.configurations;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DurationUnit;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ConfigurationProperties(prefix = "elasticsearch")
public class ElasticsearchSettings {
    private List<String> hosts = new ArrayList<>();
    private String username;
    private String password;

    @DurationUnit(ChronoUnit.MILLIS)
    private Duration connectionTimeout = Duration.ofSeconds(10);
    @DurationUnit(ChronoUnit.MILLIS)
    private Duration socketTimeout = Duration.ofSeconds(30);

    private int indigoPoolSize = 10;
    private int indigoTimeoutInSeconds = 30;
    private int inchiPoolSize = 5;
    private int inchiTimeoutInSeconds = 30;
}