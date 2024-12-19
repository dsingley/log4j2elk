package com.dsingley.log4j2elk;

import org.apache.logging.log4j.Level;
import org.junit.jupiter.api.Test;

import static com.dsingley.log4j2elk.ElkConfiguration.DEFAULT_ASYNC_QUEUE_FULL_POLICY;
import static com.dsingley.log4j2elk.Log4j2Elk.PROPERTY_ASYNC_QUEUE_FULL_POLICY;
import static com.dsingley.log4j2elk.Log4j2Elk.PROPERTY_DISCARD_THRESHOLD;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class Log4j2ElkTest {

    @Test
    void testDefaultLog4j2Properties() {
        ElkConfiguration configuration = ElkConfiguration.builder()
                .baseUrl("http://test:9200")
                .indexName("test")
                .shutdownTimeoutMs(1)
                .build();

        Log4j2Elk.configure(configuration);

        assertAll(
                () -> assertThat(System.getProperty(PROPERTY_ASYNC_QUEUE_FULL_POLICY)).isEqualTo(DEFAULT_ASYNC_QUEUE_FULL_POLICY),
                () -> assertThat(System.getProperty(PROPERTY_DISCARD_THRESHOLD)).isEqualTo("INFO")
        );
    }

    @Test
    void testConfiguredLog4j2Properties() {
        ElkConfiguration configuration = ElkConfiguration.builder()
                .baseUrl("http://test:9200")
                .indexName("test")
                .shutdownTimeoutMs(1)
                .asyncQueueFullPolicy("package.ClassThatImplementsAsyncQueueFullPolicy")
                .discardThreshold(Level.WARN)
                .build();

        Log4j2Elk.configure(configuration);

        assertAll(
                () -> assertThat(System.getProperty(PROPERTY_ASYNC_QUEUE_FULL_POLICY)).isEqualTo("package.ClassThatImplementsAsyncQueueFullPolicy"),
                () -> assertThat(System.getProperty(PROPERTY_DISCARD_THRESHOLD)).isEqualTo("WARN")
        );
    }
}
