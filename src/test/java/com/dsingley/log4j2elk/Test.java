package com.dsingley.log4j2elk;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

@Slf4j
public class Test {

    public static void main(String[] args) {
        Log4j2Elk.configure(new EnviornmentVariableElkConfigurationProvider(Test.class, "test"));

        try (MDC.MDCCloseable mdcCloseable = MDC.putCloseable("key1", "value1")) {
            for (int i = 0; i < 2; i++) {
                log.info("hello, {}", i);
            }
        }
    }
}
