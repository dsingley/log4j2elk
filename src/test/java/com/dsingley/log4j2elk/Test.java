package com.dsingley.log4j2elk;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.slf4j.event.Level;
import test.one.One;
import test.two.Two;

import java.util.Map;

import static com.dsingley.log4j2elk.EnviornmentVariableElkConfigurationProvider.FIELD_INSTANCE;

@Slf4j
public class Test {

    public static void main(String[] args) {
        ElkConfiguration elkConfiguration = Log4j2Elk.configure(new EnviornmentVariableElkConfigurationProvider(Test.class, "test"));
        log.info("{}: {}", FIELD_INSTANCE, elkConfiguration.getValue(FIELD_INSTANCE));

        for (int i = 0; i < 2; i++) {
            try (MDC.MDCCloseable mdcCloseable = MDC.putCloseable("i", String.valueOf(i))) {
                log.debug("DEBUG messages should only be logged from Test");
                One.sayHello(Level.DEBUG);
                Two.sayHello(Level.DEBUG);
                log.info("INFO messages should be logged from Test and One");
                One.sayHello(Level.INFO);
                Two.sayHello(Level.INFO);
                log.warn("WARN messages should be logged from Test, One, and Two");
                One.sayHello(Level.WARN);
                Two.sayHello(Level.WARN);
            }
        }
    }
}
