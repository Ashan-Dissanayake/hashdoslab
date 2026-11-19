
package io.github.ashan.hashdoslab.protection.ratelimit;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.Refill;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final String PROTECTED_ENDPOINT =
            "/api/v1/analysis/compare-engines";

    private static final int MAX_TRACKED_CLIENTS = 10_000;

    private static final Duration CLIENT_IDLE_TIMEOUT =
            Duration.ofMinutes(10);

    private final Cache<String, Bucket> buckets = Caffeine.newBuilder()
            .maximumSize(MAX_TRACKED_CLIENTS)
            .expireAfterAccess(CLIENT_IDLE_TIMEOUT)
            .build();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {

        String requestPath = request.getRequestURI()
                .substring(request.getContextPath().length());

        return !PROTECTED_ENDPOINT.equals(requestPath)
                || !"POST".equalsIgnoreCase(request.getMethod());
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String clientIp = request.getRemoteAddr();

        Bucket bucket = buckets.get(
                clientIp,
                ignored -> createBucket()
        );

        ConsumptionProbe probe =
                bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            filterChain.doFilter(request, response);
            return;
        }

        long waitNanos = probe.getNanosToWaitForRefill();

        long retryAfterSeconds = Math.max(
                1,
                (waitNanos + 999_999_999L) / 1_000_000_000L
        );

        writeRateLimitResponse(response, retryAfterSeconds);
    }

    private Bucket createBucket() {

        Bandwidth limit = Bandwidth.classic(
                RateLimitPolicy.CAPACITY,
                Refill.greedy(
                        RateLimitPolicy.REFILL_TOKENS,
                        Duration.ofSeconds(
                                RateLimitPolicy.REFILL_PERIOD_SECONDS
                        )
                )
        );

        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    private void writeRateLimitResponse(
            HttpServletResponse response,
            long retryAfterSeconds
    ) throws IOException {

        response.setStatus(429);
        response.setContentType("application/problem+json");
        response.setCharacterEncoding("UTF-8");

        response.setHeader(
                "Retry-After",
                String.valueOf(retryAfterSeconds)
        );

        response.getWriter().write("""
                {
                    "type": "https://hashdoslab.dev/problems/rate-limit-exceeded",
                    "title": "Too Many Requests",
                    "status": 429,
                    "detail": "Request rate limit exceeded. Please try again later."
                }
                """);
    }
}
