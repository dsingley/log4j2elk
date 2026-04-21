package com.dsingley.log4j2elk;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Singular;
import org.apache.logging.log4j.Level;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;

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

    @Getter boolean enabled;
    @Getter String baseUrl;
    String apiKey;
    String apiKeyId;
    String truststorePath;
    String truststorePassword;
    @Getter String indexName;
    @Getter Map<String, String> additionalFields;
    @Getter int connectTimeoutMs;
    @Getter int readTimeoutMs;
    @Getter boolean blocking;
    @Getter int bufferSize;
    @Getter int shutdownTimeoutMs;
    @Getter String asyncQueueFullPolicy;
    @Getter Level discardThreshold;

    @Builder(toBuilder = true)
    public ElkConfiguration(
            Boolean enabled,
            @NonNull String baseUrl,
            String apiKey,
            String truststorePath,
            String truststorePassword,
            @NonNull String indexName,
            @Singular Map<String, String> additionalFields,
            Integer connectTimeoutMs,
            Integer readTimeoutMs,
            Boolean blocking,
            Integer bufferSize,
            Integer shutdownTimeoutMs,
            String asyncQueueFullPolicy,
            Level discardThreshold
    ) {
        this.enabled = enabled != null ? enabled : DEFAULT_ENABLED;
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
        this.apiKeyId = validateApiKeyAndExtractId(apiKey);
        this.truststorePath = truststorePath;
        this.truststorePassword = truststorePassword;
        this.indexName = indexName;
        this.additionalFields = additionalFields;
        this.connectTimeoutMs = connectTimeoutMs != null ? connectTimeoutMs : DEFAULT_CONNECT_TIMEOUT_MS;
        this.readTimeoutMs = readTimeoutMs != null ? readTimeoutMs : DEFAULT_READ_TIMEOUT_MS;
        this.blocking = blocking != null ? blocking : DEFAULT_BLOCKING;
        this.bufferSize = bufferSize != null ? bufferSize : DEFAULT_BUFFER_SIZE;
        this.shutdownTimeoutMs = shutdownTimeoutMs != null ? shutdownTimeoutMs : DEFAULT_SHUTDOWN_TIMEOUT_MS;
        this.asyncQueueFullPolicy = asyncQueueFullPolicy != null ? asyncQueueFullPolicy : DEFAULT_ASYNC_QUEUE_FULL_POLICY;
        this.discardThreshold = discardThreshold != null ? discardThreshold : DEFAULT_DISCARD_THRESHOLD;
    }

    public boolean isSecure() {
        return baseUrl.startsWith("https://");
    }

    public Optional<String> getApiKey() {
        return Optional.ofNullable(apiKey);
    }

    public Optional<String> getApiKeyId() {
        return Optional.ofNullable(apiKeyId);
    }

    public Optional<String> getTruststorePath() {
        return Optional.ofNullable(truststorePath);
    }

    public Optional<String> getTruststorePassword() {
        return Optional.ofNullable(truststorePassword);
    }

    public String getValue(String key) {
        return additionalFields.entrySet().stream()
                .filter(entry -> entry.getKey().equals(key))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }

    // visible for testing
    static String validateApiKeyAndExtractId(String apiKey) throws IllegalArgumentException {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            return null;
        }
        String decodedApiKey;
        try {
            decodedApiKey = new String(Base64.getDecoder().decode(apiKey), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("apiKey is not a base64 encoded string");
        }
        String[] split = decodedApiKey.split(":");
        if (split.length == 2) {
            return split[0];
        }
        throw new IllegalArgumentException("base64 decoded apiKey does not have two parts separated by a colon");
    }
}
