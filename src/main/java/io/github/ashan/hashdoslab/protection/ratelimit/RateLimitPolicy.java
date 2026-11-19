package io.github.ashan.hashdoslab.protection.ratelimit;

public final class RateLimitPolicy {

    private RateLimitPolicy() {
    }

    public static final long CAPACITY = 5;

    public static final long REFILL_TOKENS = 10;

    public static final long REFILL_PERIOD_SECONDS = 60;

}