package com.dsingley.log4j2elk;

import lombok.extern.slf4j.Slf4j;
import mockwebserver3.Dispatcher;
import mockwebserver3.MockResponse;
import mockwebserver3.MockWebServer;
import mockwebserver3.RecordedRequest;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

@Slf4j
public abstract class BaseLog4j2ElkIntegrationTest {
    protected MockWebServer mockWebServer;

    @BeforeEach
    void setUp() {
        mockWebServer = new MockWebServer();
        mockWebServer.setDispatcher(new Dispatcher() {
            @NotNull
            @Override
            public MockResponse dispatch(@NotNull RecordedRequest recordedRequest) {
                System.out.println(recordedRequest.getRequestLine());
                System.out.println(recordedRequest.getHeaders());
                System.out.println(recordedRequest.getBody().readUtf8());
                return new MockResponse.Builder().code(200).build();
            }
        });
    }

    @AfterEach
    void tearDown() throws Exception {
        Thread.sleep(1000); // wait for asynchronous log messages to be flushed
        if (mockWebServer != null) {
            mockWebServer.shutdown();
        }
    }

    protected String getBaseUrl() {
        return mockWebServer.url("/").toString().replaceAll("/$", "");
    }
}
