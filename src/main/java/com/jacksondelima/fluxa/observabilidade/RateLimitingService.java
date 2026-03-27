package com.jacksondelima.fluxa.observabilidade;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitingService {

    private static final int MAX_ENTRIES_BEFORE_CLEANUP = 10_000;

    private final ConcurrentHashMap<String, WindowCounter> counters = new ConcurrentHashMap<>();

    public RateLimitDecision tryConsume(String bucket, String identifier, int maxRequests, Duration window) {
        cleanupIfNeeded();

        long now = System.currentTimeMillis();
        long windowMillis = window.toMillis();
        String key = bucket + ":" + identifier;

        WindowCounter counter = counters.computeIfAbsent(key, ignored -> new WindowCounter(now + windowMillis));

        synchronized (counter) {
            if (now >= counter.windowEndsAt) {
                counter.windowEndsAt = now + windowMillis;
                counter.requests = 0;
            }

            if (counter.requests >= maxRequests) {
                long retryAfterSeconds = Math.max(1, (counter.windowEndsAt - now + 999) / 1000);
                return RateLimitDecision.denied(retryAfterSeconds);
            }

            counter.requests++;
            return RateLimitDecision.allowed(maxRequests - counter.requests);
        }
    }

    public void clear() {
        counters.clear();
    }

    private void cleanupIfNeeded() {
        if (counters.size() < MAX_ENTRIES_BEFORE_CLEANUP) {
            return;
        }

        long now = System.currentTimeMillis();
        Iterator<Map.Entry<String, WindowCounter>> iterator = counters.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, WindowCounter> entry = iterator.next();
            if (entry.getValue().windowEndsAt < now) {
                iterator.remove();
            }
        }
    }

    private static final class WindowCounter {
        private long windowEndsAt;
        private int requests;

        private WindowCounter(long windowEndsAt) {
            this.windowEndsAt = windowEndsAt;
        }
    }
}
