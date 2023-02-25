package com.dsingley.log4j2elk;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.slf4j.event.Level;
import test.one.One;
import test.two.Two;

@Slf4j
public class Test {

    public static void main(String[] args) {
        Log4j2Elk.configure(new EnvironmentVariableElkConfigurationProvider(Test.class, "test"));

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
