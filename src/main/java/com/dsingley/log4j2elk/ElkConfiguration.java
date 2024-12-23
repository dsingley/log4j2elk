package com.dsingley.log4j2elk;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Singular;
import org.apache.logging.log4j.Level;

import java.util.Map;

@Builder
@Getter
public class ElkConfiguration {
    public static final boolean DEFAULT_ENABLED = true;

    // see https://logging.apache.org/log4j/2.x/manual/appenders/network.html#HttpAppender
    public static final int DEFAULT_CONNECT_TIMEOUT_MS = 10_000; // log4j2 default = 0
    public static final int DEFAULT_READ_TIMEOUT_MS = 10_000; // log4j2 default = 0

    // see https://logging.apache.org/log4j/2.x/manual/appenders/delegating.html#AsyncAppender
    public static final boolean DEFAULT_BLOCKING = true;
    public static final int DEFAULT_BUFFER_SIZE = 1024;
    public static final int DEFAULT_SHUTDOWN_TIMEOUT_MS = 30_000; // log4j2 default = 0

    // see https://logging.apache.org/log4j/2.x/manual/systemproperties.html#properties-async
    public static final String ASYNC_QUEUE_FULL_POLICY_DEFAULT = "Default";
    public static final String ASYNC_QUEUE_FULL_POLICY_DISCARD = "Discard";
    public static final String ASYNC_QUEUE_FULL_POLICY_CUSTOM_DISCARD = CustomDiscardingAsyncQueueFullPolicy.class.getName();
    public static final String DEFAULT_ASYNC_QUEUE_FULL_POLICY = ASYNC_QUEUE_FULL_POLICY_CUSTOM_DISCARD; // log4j2 default = Default
    public static final Level DEFAULT_DISCARD_THRESHOLD = Level.INFO;

    @Builder.Default boolean enabled = DEFAULT_ENABLED;
    @NonNull String baseUrl;
    @NonNull String indexName;
    @Singular Map<String, String> additionalFields;
    @Builder.Default int connectTimeoutMs = DEFAULT_CONNECT_TIMEOUT_MS;
    @Builder.Default int readTimeoutMs = DEFAULT_READ_TIMEOUT_MS;
    @Builder.Default boolean blocking = DEFAULT_BLOCKING;
    @Builder.Default int bufferSize = DEFAULT_BUFFER_SIZE;
    @Builder.Default int shutdownTimeoutMs = DEFAULT_SHUTDOWN_TIMEOUT_MS;
    @Builder.Default String asyncQueueFullPolicy = DEFAULT_ASYNC_QUEUE_FULL_POLICY;
    @Builder.Default Level discardThreshold = DEFAULT_DISCARD_THRESHOLD;

    public String getValue(String key) {
        return additionalFields.entrySet().stream()
                .filter(entry -> entry.getKey().equals(key))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }
}
