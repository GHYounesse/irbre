package com.irbre.agent.util;

import java.util.concurrent.atomic.AtomicLong;

public class LamportClock {
    private static final AtomicLong counter = new AtomicLong(0);

    // Increment and return next Lamport clock value
    public static long next() {
        return counter.incrementAndGet();
    }

    // Optional: update clock from external value (for multi-service)
    public static void update(long receivedClock) {
        counter.updateAndGet(current -> Math.max(current, receivedClock));
    }
}
