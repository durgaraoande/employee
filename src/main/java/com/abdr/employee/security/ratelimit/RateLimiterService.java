package com.abdr.employee.security.ratelimit;

import com.abdr.employee.utils.RateLimitExceededException;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Refill;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
public class RateLimiterService {
    private final Cache<String, Bucket> buckets;

    public RateLimiterService() {
        Bandwidth limit = Bandwidth.classic(5, Refill.greedy(5, Duration.ofMinutes(1))); // 5 requests per minute

        // Using the new Bucket.builder() method
        buckets = Caffeine.newBuilder()
                .expireAfterWrite(1, TimeUnit.HOURS)
                .build(key -> Bucket.builder()
                        .addLimit(limit)
                        .build());
    }

    public boolean tryConsume(String key) {
        Bucket bucket = buckets.get(key, k -> Bucket.builder()
                .addLimit(Bandwidth.classic(5, Refill.greedy(5, Duration.ofMinutes(1))))
                .build());
        return bucket.tryConsume(1);
    }

    public void checkRateLimit(String key, String action) {
        String cacheKey = String.format("%s_%s", action, key);
        if (!tryConsume(cacheKey)) {
            throw new RateLimitExceededException(
                    "Too many attempts. Please try again later.");
        }
    }
}
