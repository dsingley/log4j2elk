package com.dsingley.log4j2elk;

import lombok.Getter;
import lombok.NonNull;
import org.apache.logging.log4j.Level;

import java.util.UUID;
import java.util.function.Function;

import static com.dsingley.log4j2elk.ElkConfiguration.*;

/**
 * Provide {@link ElkConfiguration} with <code>baseUrl</code> from the environment variable
 * <code>ELK_ELASTICSEARCH_BASE_URL</code> and with <code>indexName</code>, and <code>additionalFields</code>
 * based on the specified service name and specified (or default) environment and instance values.
 * <p>
 * <code>enabled</code> unless the environment variable <code>ELK_ENABLED</code> is set
 * and not equal to <code>true</code>.
 */
public class EnvironmentVariableElkConfigurationProvider implements ElkConfigurationProvider {
    private static final String ENABLED = "ELK_ENABLED";
    private static final String ELASTICSEARCH_BASE_URL = "ELK_ELASTICSEARCH_BASE_URL";
    private static final String DEFAULT_ELASTICSEARCH_BASE_URL = "http://localhost:9200";
    private static final String ENVIRONMENT = "ELK_ENVIRONMENT";
    private static final String DEFAULT_ENVIRONMENT = "production";
    public static final String FIELD_SERVICE = "service";
    public static final String FIELD_ENVIRONMENT = "environment";
    public static final String FIELD_INSTANCE = "instance";

    private static final String CONNECT_TIMEOUT_MS = "ELK_CONNECT_TIMEOUT_MS";
    private static final String READ_TIMEOUT_MS = "ELK_READ_TIMEOUT_MS";
    private static final String BLOCKING = "ELK_BLOCKING";
    private static final String BUFFER_SIZE = "ELK_BUFFER_SIZE";
    private static final String SHUTDOWN_TIMEOUT_MS = "ELK_SHUTDOWN_TIMEOUT_MS";
    private static final String ASYNC_QUEUE_FULL_POLICY = "ELK_ASYNC_QUEUE_FULL_POLICY";
    private static final String DISCARD_THRESHOLD = "ELK_DISCARD_THRESHOLD";

    @Getter private final ElkConfiguration elkConfiguration;

    /**
     * @param clazz a class whose package name will be the name of the service
     */
    public EnvironmentVariableElkConfigurationProvider(@NonNull Class<?> clazz) {
        this(clazz.getPackage().getName(), null);
    }

    /**
     * @param service the name of the service
     */
    public EnvironmentVariableElkConfigurationProvider(@NonNull String service) {
        this(service, null);
    }

    /**
     * @param clazz       a class whose package name will be the name of the service
     * @param environment the name of the environment
     */
    public EnvironmentVariableElkConfigurationProvider(@NonNull Class<?> clazz, String environment) {
        this(clazz.getPackage().getName(), environment, null);
    }

    /**
     * @param service     the name of the service
     * @param environment the name of the environment
     */
    public EnvironmentVariableElkConfigurationProvider(@NonNull String service, String environment) {
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
    public EnvironmentVariableElkConfigurationProvider(@NonNull String service, String environment, String instance) {
        String baseUrl = System.getenv(ELASTICSEARCH_BASE_URL);
        if (baseUrl == null || baseUrl.trim().length() == 0) {
            baseUrl = DEFAULT_ELASTICSEARCH_BASE_URL;
        }

        if (environment == null || environment.trim().length() == 0) {
            environment = System.getenv(ENVIRONMENT);
            if (environment == null || environment.trim().length() == 0) {
                environment = DEFAULT_ENVIRONMENT;
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
                .enabled(getOrDefault(ENABLED, DEFAULT_ENABLED, Boolean::parseBoolean))
                .baseUrl(baseUrl)
                .indexName(indexName)
                .additionalField(FIELD_SERVICE, service)
                .additionalField(FIELD_ENVIRONMENT, environment)
                .additionalField(FIELD_INSTANCE, instance)
                .connectTimeoutMs(getOrDefault(CONNECT_TIMEOUT_MS, DEFAULT_CONNECT_TIMEOUT_MS, Integer::parseInt))
                .readTimeoutMs(getOrDefault(READ_TIMEOUT_MS, DEFAULT_READ_TIMEOUT_MS, Integer::parseInt))
                .blocking(getOrDefault(BLOCKING, DEFAULT_BLOCKING, Boolean::parseBoolean))
                .bufferSize(getOrDefault(BUFFER_SIZE, DEFAULT_BUFFER_SIZE, Integer::parseInt))
                .shutdownTimeoutMs(getOrDefault(SHUTDOWN_TIMEOUT_MS, DEFAULT_SHUTDOWN_TIMEOUT_MS, Integer::parseInt))
                .asyncQueueFullPolicy(getOrDefault(ASYNC_QUEUE_FULL_POLICY, DEFAULT_ASYNC_QUEUE_FULL_POLICY, Function.identity()))
                .discardThreshold(getOrDefault(DISCARD_THRESHOLD, DEFAULT_DISCARD_THRESHOLD, Level::getLevel))
                .build();
    }

    private static <T> T getOrDefault(String name, T defaultValue, Function<String,T> conversionFunction) {
        String value = System.getenv(name);
        if (value != null && !value.trim().isEmpty()) {
            return conversionFunction.apply(value);
        }
        return defaultValue;
    }
}
