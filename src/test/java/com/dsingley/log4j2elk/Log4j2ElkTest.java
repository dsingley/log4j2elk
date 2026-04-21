package com.dsingley.log4j2elk;

import com.dsingley.testpki.KeyType;
import com.dsingley.testpki.TestPKI;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.net.ssl.SslConfiguration;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.dsingley.log4j2elk.ElkConfiguration.DEFAULT_ASYNC_QUEUE_FULL_POLICY;
import static com.dsingley.log4j2elk.ElkConfiguration.DEFAULT_DISCARD_THRESHOLD;
import static com.dsingley.log4j2elk.Log4j2Elk.PROPERTY_ASYNC_QUEUE_FULL_POLICY;
import static com.dsingley.log4j2elk.Log4j2Elk.PROPERTY_DISCARD_THRESHOLD;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class Log4j2ElkTest {
    private static final TestPKI TEST_PKI = new TestPKI(KeyType.RSA_2048, null);

    @Nested
    class Log4jProperties {

        @Test
        void system_properties_are_our_defaults() {
            ElkConfiguration configuration = ElkConfiguration.builder()
                    .baseUrl("http://test:9200")
                    .indexName("test")
                    .shutdownTimeoutMs(1)
                    .build();

            Log4j2Elk.configure(configuration);

            assertAll(
                    () -> assertThat(System.getProperty(PROPERTY_ASYNC_QUEUE_FULL_POLICY)).isEqualTo(DEFAULT_ASYNC_QUEUE_FULL_POLICY),
                    () -> assertThat(System.getProperty(PROPERTY_DISCARD_THRESHOLD)).isEqualTo(DEFAULT_DISCARD_THRESHOLD.name())
            );

            Log4j2Elk.unconfigure();
        }

        @Test
        void system_properties_are_set_to_configuration_values() {
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

            Log4j2Elk.unconfigure();
        }
    }

    @Nested
    class NewSslConfiguration {

        @Test
        void should_return_null_when_trustStorePath_is_not_set() {
            ElkConfiguration configuration = ElkConfiguration.builder()
                    .baseUrl("https://secure:9200")
                    .indexName("test")
                    .shutdownTimeoutMs(1)
                    .build();

            SslConfiguration sslConfiguration = Log4j2Elk.newSslConfiguration(configuration);

            assertThat(sslConfiguration).isNull();
        }

        @Test
        void should_return_null_when_trustStorePath_is_set_but_baseUrl_is_not_https() {
            ElkConfiguration configuration = ElkConfiguration.builder()
                    .baseUrl("http://insecure:9200")
                    .truststorePath(TEST_PKI.getOrCreateTruststoreFile().getAbsolutePath())
                    .indexName("test")
                    .shutdownTimeoutMs(1)
                    .build();

            SslConfiguration sslConfiguration = Log4j2Elk.newSslConfiguration(configuration);

            assertThat(sslConfiguration).isNull();
        }

        @Test
        void should_return_configuration_when_trustStorePath_is_set() {
            ElkConfiguration configuration = ElkConfiguration.builder()
                    .baseUrl("https://secure:9200")
                    .truststorePath(TEST_PKI.getOrCreateTruststoreFile().getAbsolutePath())
                    .indexName("test")
                    .shutdownTimeoutMs(1)
                    .build();

            SslConfiguration sslConfiguration = Log4j2Elk.newSslConfiguration(configuration);

            assertAll(
                    () -> assertThat(sslConfiguration).isNotNull(),
                    () -> assertThat(sslConfiguration.getTrustStoreConfig().getLocation()).isEqualTo(TEST_PKI.getOrCreateTruststoreFile().getAbsolutePath()),
                    () -> assertThat(sslConfiguration.getTrustStoreConfig().getPasswordAsCharArray()).isNull()
            );
        }

        @Test
        void should_return_configuration_including_password_when_trustStorePath_and_truststorePassword_are_set() {
            ElkConfiguration configuration = ElkConfiguration.builder()
                    .baseUrl("https://secure:9200")
                    .truststorePath(TEST_PKI.getOrCreateTruststoreFile().getAbsolutePath())
                    .truststorePassword(TEST_PKI.getTruststorePassword())
                    .indexName("test")
                    .shutdownTimeoutMs(1)
                    .build();

            SslConfiguration sslConfiguration = Log4j2Elk.newSslConfiguration(configuration);

            assertAll(
                    () -> assertThat(sslConfiguration).isNotNull(),
                    () -> assertThat(sslConfiguration.getTrustStoreConfig().getLocation()).isEqualTo(TEST_PKI.getOrCreateTruststoreFile().getAbsolutePath()),
                    () -> assertThat(sslConfiguration.getTrustStoreConfig().getPasswordAsCharArray()).isEqualTo(TEST_PKI.getTruststorePassword().toCharArray())
            );
        }
    }
}
