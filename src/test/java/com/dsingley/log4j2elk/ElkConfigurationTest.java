package com.dsingley.log4j2elk;

import org.junit.jupiter.api.Test;

import static com.dsingley.log4j2elk.ElkConfiguration.*;
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
                () -> assertThat(configuration.isEnabled()).isEqualTo(DEFAULT_ENABLED),
                () -> assertThat(configuration.getAdditionalFields()).isEmpty(),
                () -> assertThat(configuration.getConnectTimeoutMs()).isEqualTo(DEFAULT_CONNECT_TIMEOUT_MS),
                () -> assertThat(configuration.getReadTimeoutMs()).isEqualTo(DEFAULT_READ_TIMEOUT_MS),
                () -> assertThat(configuration.isBlocking()).isEqualTo(DEFAULT_BLOCKING),
                () -> assertThat(configuration.getBufferSize()).isEqualTo(DEFAULT_BUFFER_SIZE),
                () -> assertThat(configuration.getShutdownTimeoutMs()).isEqualTo(DEFAULT_SHUTDOWN_TIMEOUT_MS),
                () -> assertThat(configuration.getAsyncQueueFullPolicy()).isEqualTo(DEFAULT_ASYNC_QUEUE_FULL_POLICY),
                () -> assertThat(configuration.getDiscardThreshold().name()).isEqualTo(DEFAULT_DISCARD_THRESHOLD.name())
        );
    }
}
