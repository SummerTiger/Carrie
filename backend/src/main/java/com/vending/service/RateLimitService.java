package com.vending.service;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for IP-based rate limiting to prevent brute force attacks.
 * Uses Bucket4j for token bucket algorithm implementation.
 */
@Service
public class RateLimitService {

    private final Map<String, Bucket> ipBucketCache = new ConcurrentHashMap<>();

    @Value("${app.security.rate-limit.login-requests-per-minute}")
    private int loginRequestsPerMinute;

    @Value("${app.security.rate-limit.login-requests-window-seconds}")
    private int loginRequestsWindowSeconds;

    /**
     * Resolve a bucket for the given IP address.
     * Creates a new bucket if one doesn't exist for this IP.
     */
    public Bucket resolveBucket(String ipAddress) {
        return ipBucketCache.computeIfAbsent(ipAddress, this::newBucket);
    }

    /**
     * Create a new bucket with configured rate limits.
     * Uses token bucket algorithm: loginRequestsPerMinute tokens refilled every loginRequestsWindowSeconds.
     */
    private Bucket newBucket(String ipAddress) {
        Bandwidth limit = Bandwidth.classic(
                loginRequestsPerMinute,
                Refill.intervally(loginRequestsPerMinute, Duration.ofSeconds(loginRequestsWindowSeconds))
        );
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    /**
     * Check if a request from this IP should be allowed.
     * Consumes a token from the bucket if available.
     *
     * @param ipAddress The IP address to check
     * @return true if request is allowed, false if rate limit exceeded
     */
    public boolean tryConsume(String ipAddress) {
        Bucket bucket = resolveBucket(ipAddress);
        return bucket.tryConsume(1);
    }

    /**
     * Clear the rate limit cache for a specific IP (e.g., after successful login).
     *
     * @param ipAddress The IP address to reset
     */
    public void resetRateLimit(String ipAddress) {
        ipBucketCache.remove(ipAddress);
    }
}
