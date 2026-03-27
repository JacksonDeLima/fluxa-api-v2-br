package com.jacksondelima.fluxa.observabilidade;

public record RateLimitDecision(
        boolean allowed,
        int remainingRequests,
        long retryAfterSeconds
) {

    public static RateLimitDecision allowed(int remainingRequests) {
        return new RateLimitDecision(true, remainingRequests, 0);
    }

    public static RateLimitDecision denied(long retryAfterSeconds) {
        return new RateLimitDecision(false, 0, retryAfterSeconds);
    }
}
