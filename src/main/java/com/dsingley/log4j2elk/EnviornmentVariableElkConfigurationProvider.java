package com.dsingley.log4j2elk;

import lombok.Getter;
import lombok.NonNull;

import java.util.UUID;

public class EnviornmentVariableElkConfigurationProvider implements ElkConfigurationProvider {
    private static final String ELK_ELASTICSEARCH_BASE_URL = "ELK_ELASTICSEARCH_BASE_URL";
    private static final String DEFAULT_ELK_ELASTICSEARCH_BASE_URL = "http://localhost:9200";

    @Getter private final ElkConfiguration elkConfiguration;

    public EnviornmentVariableElkConfigurationProvider(@NonNull Class<?> clazz, @NonNull String environment) {
        this(clazz.getPackage().getName(), environment, UUID.randomUUID().toString().replace("-", ""));
    }

    public EnviornmentVariableElkConfigurationProvider(@NonNull String service, @NonNull String environment, @NonNull String instance) {
        String baseUrl = System.getenv(ELK_ELASTICSEARCH_BASE_URL);
        if (baseUrl == null || baseUrl.trim().length() == 0) {
            baseUrl = DEFAULT_ELK_ELASTICSEARCH_BASE_URL;
        }

        String indexName = String.format(
                "logs-%s-%s",
                service.replaceAll("(?i)[^a-z0-9.]", "").toLowerCase(),
                environment.replaceAll("(?i)[^a-z0-9.]", "").toLowerCase()
        );

        elkConfiguration = ElkConfiguration.builder()
                .baseUrl(baseUrl)
                .indexName(indexName)
                .additionalField("service", service)
                .additionalField("environment", environment)
                .additionalField("instance", instance)
                .build();
    }
}
