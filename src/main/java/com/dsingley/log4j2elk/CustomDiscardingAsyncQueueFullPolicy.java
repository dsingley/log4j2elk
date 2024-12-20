package com.dsingley.log4j2elk;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.async.DefaultAsyncQueueFullPolicy;
import org.apache.logging.log4j.core.async.DiscardingAsyncQueueFullPolicy;
import org.apache.logging.log4j.core.async.EventRoute;
import org.apache.logging.log4j.status.StatusLogger;
import org.apache.logging.log4j.util.PropertiesUtil;

import java.util.concurrent.atomic.AtomicLong;

import static com.dsingley.log4j2elk.ElkConfiguration.DEFAULT_DISCARD_THRESHOLD;
import static com.dsingley.log4j2elk.Log4j2Elk.PROPERTY_DISCARD_THRESHOLD;

/**
 * Based on {@link DiscardingAsyncQueueFullPolicy} but will periodically log a warning if the queue is full.
 */
public class CustomDiscardingAsyncQueueFullPolicy extends DefaultAsyncQueueFullPolicy {
    public static final long WARNING_INTERVAL_SECONDS = 60;

    private final Level thresholdLevel;
    private final AtomicLong lastWarningTimeMs;

    public CustomDiscardingAsyncQueueFullPolicy() {
        PropertiesUtil util = PropertiesUtil.getProperties();
        String level = util.getStringProperty(PROPERTY_DISCARD_THRESHOLD, DEFAULT_DISCARD_THRESHOLD.name());
        thresholdLevel = Level.toLevel(level, DEFAULT_DISCARD_THRESHOLD);
        lastWarningTimeMs = new AtomicLong(0);
    }

    @Override
    public EventRoute getRoute(final long backgroundThreadId, final Level level) {
        if (level.isLessSpecificThan(thresholdLevel)) {
            logQueueFullWarning(level);
            return EventRoute.DISCARD;
        }
        return super.getRoute(backgroundThreadId, level);
    }

    private void logQueueFullWarning(final Level level) {
        long currentTimeMs = System.currentTimeMillis();
        if (currentTimeMs - lastWarningTimeMs.get() >= WARNING_INTERVAL_SECONDS * 1000) {
            synchronized (this) {
                if (currentTimeMs - lastWarningTimeMs.get() >= WARNING_INTERVAL_SECONDS * 1000) {
                    lastWarningTimeMs.set(currentTimeMs);
                    StatusLogger.getLogger().warn(
                            "Async queue is full, discarding event with level {}."
                                    + " This message will only appear once every {} seconds;"
                                    + " events <= {} will be silently discarded until queue capacity becomes available.",
                            level,
                            WARNING_INTERVAL_SECONDS,
                            thresholdLevel
                    );
                }
            }
        }
    }
}
