package com.dsingley.log4j2elk;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AsyncAppender;
import org.apache.logging.log4j.core.appender.HttpAppender;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.layout.template.json.JsonTemplateLayout;

import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@UtilityClass
public class Log4j2Elk {

    public ElkConfiguration configure(ElkConfigurationProvider elkConfigurationProvider) {
        return configure(elkConfigurationProvider.getElkConfiguration());
    }

    @SneakyThrows
    public ElkConfiguration configure(ElkConfiguration elkConfiguration) {
        URL url = new URL(String.format("%s/%s/_doc/", elkConfiguration.getBaseUrl(), elkConfiguration.getIndexName()));

        LoggerContext loggerContext = LoggerContext.getContext(false);
        Configuration configuration = loggerContext.getConfiguration();

        Appender httpElasticsearchAppender = HttpAppender.newBuilder()
                .setName("httpElasticsearchAppender")
                .setConfiguration(configuration)
                .setUrl(url)
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
        return elkConfiguration;
    }
}
