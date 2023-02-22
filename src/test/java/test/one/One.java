package test.one;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.event.Level;

@Slf4j
@UtilityClass
public class One {

    public static void sayHello(Level level) {
        log.atLevel(level).log("hello from One at level {}", level);
    }
}
