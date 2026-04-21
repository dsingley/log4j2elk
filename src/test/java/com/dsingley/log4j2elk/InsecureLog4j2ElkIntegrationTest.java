package com.dsingley.log4j2elk;

import lombok.extern.slf4j.Slf4j;
import mockwebserver3.RecordedRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Base64;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@Slf4j
public class InsecureLog4j2ElkIntegrationTest extends BaseLog4j2ElkIntegrationTest {

    @BeforeEach
    void setUp() throws Exception {
        super.setUp();
        mockWebServer.start(0);
    }

    @Test
    void should_post_to_elasticsearch() throws Exception {
        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();

        ElkConfiguration configuration = ElkConfiguration.builder()
                .baseUrl(getBaseUrl())
                .indexName("insecure")
                .build();

        Log4j2Elk.configure(configuration);

        log.debug(methodName);

        RecordedRequest request = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        assertThat(request).isNotNull();

        assertAll(
                () -> assertThat(request.getMethod()).isEqualTo("POST"),
                () -> assertThat(request.getUrl().encodedPath()).isEqualTo("/insecure/_doc/")
        );
    }

    @Test
    void should_not_set_authorization_header_even_if_apiKey_is_set() throws Exception {
        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
        String apiKey = Base64.getEncoder().encodeToString("api-key-id:secret-value".getBytes());

        ElkConfiguration configuration = ElkConfiguration.builder()
                .baseUrl(getBaseUrl())
                .apiKey(apiKey)
                .indexName("insecure")
                .build();

        Log4j2Elk.configure(configuration);

        log.debug(methodName);

        RecordedRequest request = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        assertThat(request).isNotNull();

        assertAll(
                () -> assertThat(request.getMethod()).isEqualTo("POST"),
                () -> assertThat(request.getUrl().encodedPath()).isEqualTo("/insecure/_doc/"),
                () -> assertThat(request.getHeaders().get("Authorization")).isNull()
        );
    }

    @Test
    void should_include_values_from_EnvironmentVariableElkConfigurationProvider() throws Exception {
        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();

        EnvironmentVariableElkConfigurationProvider configurationProvider = new EnvironmentVariableElkConfigurationProvider("integration-test");
        ElkConfiguration configuration = configurationProvider.getElkConfiguration()
                .toBuilder()
                .baseUrl(getBaseUrl()) // override the base URL with current MockWebServer instance
                .build();

        Log4j2Elk.configure(configuration);

        log.debug(methodName);

        RecordedRequest request = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        assertThat(request).isNotNull();

        assertAll(
                () -> assertThat(request.getMethod()).isEqualTo("POST"),
                () -> assertThat(request.getUrl().encodedPath()).isEqualTo("/logs-integrationtest-production/_doc/"),
                () -> assertThat(request.getBody().utf8()).contains("\"service\":\"integration-test\""),
                () -> assertThat(request.getBody().utf8()).contains("\"environment\":\"production\""),
                () -> assertThat(request.getBody().utf8()).contains("\"instance\":\"" + configuration.getValue("instance") +"\""),
                () -> assertThat(request.getBody().utf8()).contains("\"host.ip\":\"" + configuration.getValue("host.ip") +"\""),
                () -> assertThat(request.getBody().utf8()).contains("\"host.name\":\"" + configuration.getValue("host.name") +"\"")
         );
    }
}
