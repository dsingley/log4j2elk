package com.dsingley.log4j2elk;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AsyncAppender;
import org.apache.logging.log4j.core.appender.HttpAppender;
import org.apache.logging.log4j.core.config.AbstractConfiguration;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.net.ssl.SslConfiguration;
import org.apache.logging.log4j.core.net.ssl.TrustStoreConfiguration;
import org.apache.logging.log4j.layout.template.json.JsonTemplateLayout;
import org.apache.logging.log4j.status.StatusConsoleListener;
import org.apache.logging.log4j.status.StatusLogger;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.apache.logging.log4j.Level.WARN;

@Slf4j
@UtilityClass
public class Log4j2Elk {
    private static final String HTTP_ELASTICSEARCH_APPENDER_NAME = "httpElasticsearchAppender";
    private static final String ASYNC_HTTP_ELASTICSEARCH_APPENDER_NAME = "asyncHttpElasticsearchAppender";
    static final String PROPERTY_ASYNC_QUEUE_FULL_POLICY = "log4j2.asyncQueueFullPolicy";
    static final String PROPERTY_DISCARD_THRESHOLD = "log4j2.discardThreshold";

    public ElkConfiguration configure(ElkConfigurationProvider elkConfigurationProvider) {
        return configure(elkConfigurationProvider.getElkConfiguration());
    }

    @SneakyThrows
    public ElkConfiguration configure(ElkConfiguration elkConfiguration) {
        if (!elkConfiguration.isEnabled()) {
            log.warn("sending log messages to ELK is not enabled");
            return elkConfiguration;
        }

        StatusConsoleListener statusConsoleListener = StatusLogger.getLogger().getFallbackListener();
        if (statusConsoleListener.getStatusLevel().isMoreSpecificThan(WARN)) {
            statusConsoleListener.setLevel(WARN);
        }
        System.setProperty(PROPERTY_ASYNC_QUEUE_FULL_POLICY, elkConfiguration.getAsyncQueueFullPolicy());
        System.setProperty(PROPERTY_DISCARD_THRESHOLD, elkConfiguration.getDiscardThreshold().name());

        URL url = new URL(String.format("%s/%s/_doc/", elkConfiguration.getBaseUrl(), elkConfiguration.getIndexName()));

        LoggerContext loggerContext = LoggerContext.getContext(false);
        Configuration configuration = loggerContext.getConfiguration();

        List<Property> headers = new ArrayList<>();
        elkConfiguration.getApiKey()
                .ifPresent(apiKey -> {
                    if (elkConfiguration.isSecure()) {
                        Property authorization = Property.createProperty("Authorization", String.format("ApiKey %s", apiKey));
                        headers.add(authorization);
                    } else {
                        log.warn("Authorization header with ApiKey not enabled because configuration is not secure");
                    }
                });
        Property[] headerArray = headers.toArray(new Property[0]);

        Appender httpElasticsearchAppender = HttpAppender.newBuilder()
                .setName(HTTP_ELASTICSEARCH_APPENDER_NAME)
                .setConfiguration(configuration)
                .setUrl(url)
                .setSslConfiguration(newSslConfiguration(elkConfiguration))
                .setHeaders(headerArray)
                .setConnectTimeoutMillis(elkConfiguration.getConnectTimeoutMs())
                .setReadTimeoutMillis(elkConfiguration.getReadTimeoutMs())
                .setLayout(
                        JsonTemplateLayout.newBuilder()
                                .setConfiguration(configuration)
                                .setEventTemplateUri("classpath:EcsLayout.json")
                                .setEventTemplateAdditionalFields(
                                        elkConfiguration.getAdditionalFields().entrySet().stream()
                                                .map(entry -> JsonTemplateLayout.EventTemplateAdditionalField.newBuilder()
                                                        .setKey(entry.getKey())
                                                        .setValue(entry.getValue())
                                                        .build())
                                                .toArray(JsonTemplateLayout.EventTemplateAdditionalField[]::new)
                                )
                                .build()
                )
                .build();
        httpElasticsearchAppender.start();
        configuration.addAppender(httpElasticsearchAppender);

        Appender asyncHttpElasticsearchAppender = AsyncAppender.newBuilder()
                .setName(ASYNC_HTTP_ELASTICSEARCH_APPENDER_NAME)
                .setConfiguration(configuration)
                .setBlocking(elkConfiguration.isBlocking())
                .setBufferSize(elkConfiguration.getBufferSize())
                .setShutdownTimeout(elkConfiguration.getShutdownTimeoutMs())
                .setAppenderRefs(
                        new AppenderRef[]{
                                AppenderRef.createAppenderRef(httpElasticsearchAppender.getName(), null, null)
                        }
                )
                .build();
        asyncHttpElasticsearchAppender.start();
        configuration.addAppender(asyncHttpElasticsearchAppender);

        configuration.getLoggers().values().forEach(loggerConfig ->
                loggerConfig.addAppender(asyncHttpElasticsearchAppender, null, null)
        );
        log.info("asynchronously sending log messages to {}", url);
        elkConfiguration.getApiKeyId()
                .ifPresent(apiKeyId -> {
                    if (elkConfiguration.isSecure()) {
                        log.info("authenticating with ApiKey ID: {}", apiKeyId);
                    }
                });
        elkConfiguration.getAdditionalFields().forEach((key, value) -> log.info("{}: {}", key, value));
        return elkConfiguration;
    }

    // visible for testing
    @SneakyThrows
    static SslConfiguration newSslConfiguration(ElkConfiguration elkConfiguration) {
        if (elkConfiguration.isSecure() && elkConfiguration.getTruststorePath().isPresent()) {
            TrustStoreConfiguration trustStoreConfiguration = TrustStoreConfiguration.createKeyStoreConfiguration(
                    elkConfiguration.getTruststorePath().get(),
                    elkConfiguration.getTruststorePassword().map(String::toCharArray).orElse(null),
                    null,
                    null,
                    null,
                    null
            );
            return SslConfiguration.createSSLConfiguration(
                    null,
                    null,
                    trustStoreConfiguration
            );
        }
        return null;
    }

    // visible for testing
    static void unconfigure() {
        LoggerContext loggerContext = LoggerContext.getContext(false);
        AbstractConfiguration abstractConfiguration = (AbstractConfiguration) loggerContext.getConfiguration();
        abstractConfiguration.removeAppender(ASYNC_HTTP_ELASTICSEARCH_APPENDER_NAME);
        abstractConfiguration.removeAppender(HTTP_ELASTICSEARCH_APPENDER_NAME);
    }
}
