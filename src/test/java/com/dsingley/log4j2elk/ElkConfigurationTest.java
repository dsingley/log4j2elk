package com.dsingley.log4j2elk;

import org.junit.jupiter.api.Test;

import static com.dsingley.log4j2elk.ElkConfiguration.DEFAULT_ASYNC_QUEUE_FULL_POLICY;
import static com.dsingley.log4j2elk.ElkConfiguration.DEFAULT_CONNECT_TIMEOUT_MS;
import static com.dsingley.log4j2elk.ElkConfiguration.DEFAULT_READ_TIMEOUT_MS;
import static com.dsingley.log4j2elk.ElkConfiguration.DEFAULT_SHUTDOWN_TIMEOUT_MS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class ElkConfigurationTest {

    @Test
    void testBuilderDefaults() {
        ElkConfiguration configuration = ElkConfiguration.builder()
                .baseUrl("http://test:9200")
                .indexName("test-index-name")
                .build();

        assertAll(
                () -> assertThat(configuration.isEnabled()).isTrue(),
                () -> assertThat(configuration.getAdditionalFields()).isEmpty(),
                () -> assertThat(configuration.getConnectTimeoutMs()).isEqualTo(DEFAULT_CONNECT_TIMEOUT_MS),
                () -> assertThat(configuration.getReadTimeoutMs()).isEqualTo(DEFAULT_READ_TIMEOUT_MS),
                () -> assertThat(configuration.isBlocking()).isTrue(),
                () -> assertThat(configuration.getBufferSize()).isEqualTo(1024),
                () -> assertThat(configuration.getShutdownTimeoutMs()).isEqualTo(DEFAULT_SHUTDOWN_TIMEOUT_MS),
                () -> assertThat(configuration.getAsyncQueueFullPolicy()).isEqualTo(DEFAULT_ASYNC_QUEUE_FULL_POLICY),
                () -> assertThat(configuration.getDiscardThreshold().name()).isEqualTo("INFO")
        );
    }
}
