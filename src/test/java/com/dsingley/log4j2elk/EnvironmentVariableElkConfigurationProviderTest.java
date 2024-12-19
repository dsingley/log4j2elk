package com.dsingley.log4j2elk;

import org.apache.logging.log4j.Level;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetEnvironmentVariable;

import java.util.regex.Pattern;

import static com.dsingley.log4j2elk.ElkConfiguration.DEFAULT_ASYNC_QUEUE_FULL_POLICY;
import static com.dsingley.log4j2elk.ElkConfiguration.DEFAULT_CONNECT_TIMEOUT_MS;
import static com.dsingley.log4j2elk.ElkConfiguration.DEFAULT_READ_TIMEOUT_MS;
import static com.dsingley.log4j2elk.ElkConfiguration.DEFAULT_SHUTDOWN_TIMEOUT_MS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class EnvironmentVariableElkConfigurationProviderTest {

    @Test
    void testDefaults() {
        ElkConfigurationProvider provider = new EnvironmentVariableElkConfigurationProvider("test-service");
        ElkConfiguration configuration = provider.getElkConfiguration();

        assertAll(
                () -> assertThat(configuration.isEnabled()).isTrue(),
                () -> assertThat(configuration.getBaseUrl()).isEqualTo("http://localhost:9200"),
                () -> assertThat(configuration.getIndexName()).isEqualTo("logs-testservice-production"),
                () -> assertThat(configuration.getAdditionalFields()).hasFieldOrPropertyWithValue("service", "test-service"),
                () -> assertThat(configuration.getAdditionalFields()).hasFieldOrPropertyWithValue("environment", "production"),
                () -> assertThat(configuration.getAdditionalFields()).extracting("instance").matches(s -> Pattern.matches("[0-9a-f]{32}", (String) s)),
                () -> assertThat(configuration.getConnectTimeoutMs()).isEqualTo(DEFAULT_CONNECT_TIMEOUT_MS),
                () -> assertThat(configuration.getReadTimeoutMs()).isEqualTo(DEFAULT_READ_TIMEOUT_MS),
                () -> assertThat(configuration.isBlocking()).isTrue(),
                () -> assertThat(configuration.getBufferSize()).isEqualTo(1024),
                () -> assertThat(configuration.getShutdownTimeoutMs()).isEqualTo(DEFAULT_SHUTDOWN_TIMEOUT_MS),
                () -> assertThat(configuration.getAsyncQueueFullPolicy()).isEqualTo(DEFAULT_ASYNC_QUEUE_FULL_POLICY),
                () -> assertThat(configuration.getDiscardThreshold().name()).isEqualTo("INFO")
        );
    }

    @Test
    @SetEnvironmentVariable(key = "ELK_ENABLED", value = "true")
    void testEnabled() {
        ElkConfigurationProvider provider = new EnvironmentVariableElkConfigurationProvider("test-enabled-service");
        ElkConfiguration configuration = provider.getElkConfiguration();

        assertThat(configuration.isEnabled()).isTrue();
    }

    @Test
    @SetEnvironmentVariable(key = "ELK_ENABLED", value = "false")
    void testDisabled() {
        ElkConfigurationProvider provider = new EnvironmentVariableElkConfigurationProvider("test-disabled-service");
        ElkConfiguration configuration = provider.getElkConfiguration();

        assertThat(configuration.isEnabled()).isFalse();
    }

    @Test
    @SetEnvironmentVariable(key = "ELK_ENVIRONMENT", value = "env1")
    void testEnvironment() {
        ElkConfigurationProvider provider = new EnvironmentVariableElkConfigurationProvider("test-environment-service");
        ElkConfiguration configuration = provider.getElkConfiguration();

        assertThat(configuration.getAdditionalFields()).hasFieldOrPropertyWithValue("environment", "env1");
    }

    @Test
    @SetEnvironmentVariable(key = "ELK_CONNECT_TIMEOUT_MS", value = "1000")
    void testConnectTimeoutMs() {
        ElkConfigurationProvider provider = new EnvironmentVariableElkConfigurationProvider("test-connect-timeout-service");
        ElkConfiguration configuration = provider.getElkConfiguration();

        assertThat(configuration.getConnectTimeoutMs()).isEqualTo(1000);
    }

    @Test
    @SetEnvironmentVariable(key = "ELK_READ_TIMEOUT_MS", value = "1000")
    void testReadTimeoutMs() {
        ElkConfigurationProvider provider = new EnvironmentVariableElkConfigurationProvider("test-read-timeout-service");
        ElkConfiguration configuration = provider.getElkConfiguration();

        assertThat(configuration.getReadTimeoutMs()).isEqualTo(1000);
    }

    @Test
    @SetEnvironmentVariable(key = "ELK_BLOCKING", value = "false")
    void testBlocking() {
        ElkConfigurationProvider provider = new EnvironmentVariableElkConfigurationProvider("test-blocking-service");
        ElkConfiguration configuration = provider.getElkConfiguration();

        assertThat(configuration.isBlocking()).isFalse();
    }

    @Test
    @SetEnvironmentVariable(key = "ELK_BUFFER_SIZE", value = "4096")
    void testBufferSize() {
        ElkConfigurationProvider provider = new EnvironmentVariableElkConfigurationProvider("test-buffer-service");
        ElkConfiguration configuration = provider.getElkConfiguration();

        assertThat(configuration.getBufferSize()).isEqualTo(4096);
    }

    @Test
    @SetEnvironmentVariable(key = "ELK_SHUTDOWN_TIMEOUT_MS", value = "1000")
    void testShutdownTimeoutMs() {
        ElkConfigurationProvider provider = new EnvironmentVariableElkConfigurationProvider("test-shutdown-service");
        ElkConfiguration configuration = provider.getElkConfiguration();

        assertThat(configuration.getShutdownTimeoutMs()).isEqualTo(1000);
    }

    @Test
    @SetEnvironmentVariable(key = "ELK_ASYNC_QUEUE_FULL_POLICY", value = "package.ClassThatImplementsAsyncQueueFullPolicy")
    void testAsyncQueueFullPolicy() {
        ElkConfigurationProvider provider = new EnvironmentVariableElkConfigurationProvider("test-policy-service");
        ElkConfiguration configuration = provider.getElkConfiguration();

        assertThat(configuration.getAsyncQueueFullPolicy()).isEqualTo("package.ClassThatImplementsAsyncQueueFullPolicy");
    }

    @Test
    @SetEnvironmentVariable(key = "ELK_DISCARD_THRESHOLD", value = "WARN")
    void testDiscardThreshold() {
        ElkConfigurationProvider provider = new EnvironmentVariableElkConfigurationProvider("test-threshold-service");
        ElkConfiguration configuration = provider.getElkConfiguration();

        assertThat(configuration.getDiscardThreshold().name()).isEqualTo(Level.WARN.name());
    }
}
