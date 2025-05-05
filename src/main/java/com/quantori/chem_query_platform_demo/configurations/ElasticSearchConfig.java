package com.quantori.chem_query_platform_demo.configurations;

import co.elastic.clients.elasticsearch.ElasticsearchAsyncClient;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quantori.cqp.api.StorageConfiguration;
import com.quantori.cqp.api.indigo.IndigoFingerprintCalculator;
import com.quantori.cqp.api.indigo.IndigoInchiProvider;
import com.quantori.cqp.api.indigo.IndigoMatcher;
import com.quantori.cqp.api.indigo.IndigoProvider;
import com.quantori.cqp.api.model.MapConvertable;
import com.quantori.cqp.api.service.StorageLibrary;
import com.quantori.cqp.api.service.StorageMolecules;
import com.quantori.cqp.api.service.StorageReactions;
import com.quantori.cqp.storage.elasticsearch.ElasticsearchProperties;
import com.quantori.cqp.storage.elasticsearch.ElasticsearchStorageConfiguration;
import jakarta.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@EnableConfigurationProperties(ElasticsearchSettings.class)
public class ElasticSearchConfig implements StorageConfiguration {
    public static final String STORAGE_NAME = "cqpelasticsearch";
    private final StorageLibrary storageLibrary;
    private final StorageMolecules storageMolecules;
    private final String storageType;
    private final MapConvertable defaultServiceData;

    private final IndigoProvider indigoProvider;
    private final IndigoInchiProvider indigoInchiProvider;

    public ElasticSearchConfig(ElasticsearchSettings settings) {
        // create Indigo pools
        this.indigoProvider = new IndigoProvider(
                settings.getIndigoPoolSize(),
                settings.getIndigoTimeoutInSeconds()
        );
        this.indigoInchiProvider = new IndigoInchiProvider(
                settings.getInchiPoolSize(),
                settings.getInchiTimeoutInSeconds()
        );

        // prepare the transport-side properties for the CQP library
        ElasticsearchProperties serviceProps = new ElasticsearchProperties();
        serviceProps.setHosts(settings.getHosts());
        serviceProps.setUsername(settings.getUsername());
        serviceProps.setPassword(settings.getPassword());
        serviceProps.getConnection()
                .setConnectionTimeout(settings.getConnectionTimeout());

        // build the CQP Elastic storage configuration
        var config = new ElasticsearchStorageConfiguration(
                serviceProps,
                new IndigoMatcher(indigoProvider),
                new IndigoMatcher(indigoProvider),
                new IndigoFingerprintCalculator(indigoProvider),
                new IndigoFingerprintCalculator(indigoProvider)
        );

        this.storageLibrary = config.getStorageLibrary();
        this.storageMolecules = config.getStorageMolecules();
        this.storageType = config.storageType();
        this.defaultServiceData = config.defaultServiceData();
    }

    // ——————— Beans for Elastic clients ———————

    @Bean
    public RestClient restClient(ElasticsearchSettings settings) {
        var httpHosts = settings.getHosts().stream()
                .map(HttpHost::create)
                .toArray(HttpHost[]::new);

        RestClientBuilder builder = RestClient.builder(httpHosts)
                .setRequestConfigCallback(cfg -> cfg
                        .setConnectTimeout((int) settings.getConnectionTimeout().toMillis())
                        .setSocketTimeout((int) settings.getSocketTimeout().toMillis())
                        .setAuthenticationEnabled(StringUtils.isNotBlank(settings.getUsername()))
                );

        if (StringUtils.isNotBlank(settings.getUsername())) {
            BasicCredentialsProvider creds = new BasicCredentialsProvider();
            creds.setCredentials(AuthScope.ANY,
                    new UsernamePasswordCredentials(
                            settings.getUsername(),
                            settings.getPassword()
                    ));
            builder.setHttpClientConfigCallback(hc -> hc.setDefaultCredentialsProvider(creds));
            log.info("Elasticsearch client authentication enabled for user {}", settings.getUsername());
        }

        log.info("Connecting to Elasticsearch hosts {}", settings.getHosts());
        return builder.build();
    }

    @Bean
    public ElasticsearchTransport elasticsearchTransport(RestClient restClient) {
        return new RestClientTransport(restClient, new JacksonJsonpMapper(new ObjectMapper()));
    }

    @Bean
    public ElasticsearchClient elasticsearchClient(ElasticsearchTransport transport) {
        return new ElasticsearchClient(transport);
    }

    @Bean
    public ElasticsearchAsyncClient elasticsearchAsyncClient(ElasticsearchTransport transport) {
        return new ElasticsearchAsyncClient(transport);
    }

    // ——————— Beans for Indigo & storage ———————

    @Bean
    public IndigoProvider indigoProvider() {
        return indigoProvider;
    }

    @Bean
    public IndigoInchiProvider indigoInchiProvider() {
        return indigoInchiProvider;
    }

    @Bean
    public StorageLibrary storageLibrary() {
        return storageLibrary;
    }

    @Bean
    public StorageMolecules storageMolecules() {
        return storageMolecules;
    }

    // ——————— StorageConfiguration interface ———————

    @Override
    public StorageLibrary getStorageLibrary() {
        return storageLibrary;
    }

    @Override
    public StorageMolecules getStorageMolecules() {
        return storageMolecules;
    }

    @Nullable
    @Override
    public StorageReactions getStorageReactions() {
        return null;
    }

    @Override
    public String storageType() {
        return storageType;
    }

    @Override
    public MapConvertable defaultServiceData() {
        return defaultServiceData;
    }
}