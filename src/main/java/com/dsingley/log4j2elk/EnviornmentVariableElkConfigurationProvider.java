package com.dsingley.log4j2elk;

import lombok.Getter;
import lombok.NonNull;

import java.util.UUID;

/**
 * Provide {@link ElkConfiguration} with <code>baseUrl</code> from the environment variable
 * <code>ELK_ELASTICSEARCH_BASE_URL</code> and with <code>indexName</code>, and <code>additionalFields</code>
 * based on the specified service name and specified (or default) environment and instance values.
 */
public class EnviornmentVariableElkConfigurationProvider implements ElkConfigurationProvider {
    private static final String ELK_ELASTICSEARCH_BASE_URL = "ELK_ELASTICSEARCH_BASE_URL";
    private static final String DEFAULT_ELK_ELASTICSEARCH_BASE_URL = "http://localhost:9200";
    private static final String ELK_ENVIRONMENT = "ELK_ENVIRONMENT";
    private static final String DEFAULT_ELK_ENVIRONMENT = "production";
    public static final String FIELD_SERVICE = "service";
    public static final String FIELD_ENVIRONMENT = "environment";
    public static final String FIELD_INSTANCE = "instance";

    @Getter private final ElkConfiguration elkConfiguration;

    /**
     * @param clazz a class whose package name will be the name of the service
     */
    public EnviornmentVariableElkConfigurationProvider(@NonNull Class<?> clazz) {
        this(clazz.getPackage().getName(), null);
    }

    /**
     * @param service the name of the service
     */
    public EnviornmentVariableElkConfigurationProvider(@NonNull String service) {
        this(service, null);
    }

    /**
     * @param clazz       a class whose package name will be the name of the service
     * @param environment the name of the environment
     */
    public EnviornmentVariableElkConfigurationProvider(@NonNull Class<?> clazz, String environment) {
        this(clazz.getPackage().getName(), environment, null);
    }

    /**
     * @param service     the name of the service
     * @param environment the name of the environment
     */
    public EnviornmentVariableElkConfigurationProvider(@NonNull String service, String environment) {
        this(service, environment, null);
    }

    /**
     * Default Values:
     * <dl>
     *     <dt><code>environment</code></dt>
     *     <dd>the environment variable <code>ELK_ENVIRONMENT</code> if set, else "<code>production</code>"</dd>
     *     <dt><code>instance</code></dt>
     *     <dd>a random UUID
     * </dl>
     *
     * @param service     the name of the service
     * @param environment the name of the environment
     * @param instance    a discriminator for different sources of messages from the same service and environment
     */
    public EnviornmentVariableElkConfigurationProvider(@NonNull String service, String environment, String instance) {
        String baseUrl = System.getenv(ELK_ELASTICSEARCH_BASE_URL);
        if (baseUrl == null || baseUrl.trim().length() == 0) {
            baseUrl = DEFAULT_ELK_ELASTICSEARCH_BASE_URL;
        }

        if (environment == null || environment.trim().length() == 0) {
            environment = System.getenv(ELK_ENVIRONMENT);
            if (environment == null || environment.trim().length() == 0) {
                environment = DEFAULT_ELK_ENVIRONMENT;
            }
        }

        String indexName = String.format(
                "logs-%s-%s",
                service.replaceAll("(?i)[^a-z0-9.]", "").toLowerCase(),
                environment.replaceAll("(?i)[^a-z0-9.]", "").toLowerCase()
        );

        if (instance == null || instance.trim().length() == 0) {
            instance = UUID.randomUUID().toString().replace("-", "");
        }

        elkConfiguration = ElkConfiguration.builder()
                .baseUrl(baseUrl)
                .indexName(indexName)
                .additionalField(FIELD_SERVICE, service)
                .additionalField(FIELD_ENVIRONMENT, environment)
                .additionalField(FIELD_INSTANCE, instance)
                .build();
    }
}
