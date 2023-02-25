package test.two;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.event.Level;

@Slf4j
@UtilityClass
public class Two {

    public static void sayHello(Level level) {
        log.atLevel(level).log("hello from Two at level {}", level);
    }
}
