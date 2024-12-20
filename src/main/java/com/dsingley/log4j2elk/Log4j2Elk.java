package com.dsingley.log4j2elk;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AsyncAppender;
import org.apache.logging.log4j.core.appender.HttpAppender;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.layout.template.json.JsonTemplateLayout;
import org.apache.logging.log4j.status.StatusConsoleListener;
import org.apache.logging.log4j.status.StatusLogger;

import java.net.URL;

import static org.apache.logging.log4j.Level.WARN;

@Slf4j
@UtilityClass
public class Log4j2Elk {
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

        Appender httpElasticsearchAppender = HttpAppender.newBuilder()
                .setName("httpElasticsearchAppender")
                .setConfiguration(configuration)
                .setUrl(url)
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
                .setName("asyncHttpElasticsearchAppender")
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
        elkConfiguration.getAdditionalFields().forEach((key, value) -> log.info("{}: {}", key, value));
        return elkConfiguration;
    }
}
