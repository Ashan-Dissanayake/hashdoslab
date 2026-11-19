
package io.github.ashan.hashdoslab.protection.ratelimit;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final String PROTECTED_ENDPOINT =
            "/api/v1/analysis/compare-engines";

    private static final int MAX_TRACKED_CLIENTS = 10_000;

    private final ConcurrentHashMap<String, Bucket> buckets =
            new ConcurrentHashMap<>();

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

        System.out.println("RATE LIMIT FILTER EXECUTED");
        System.out.println("Request URI: " + request.getRequestURI());
        System.out.println("Servlet Path: " + request.getServletPath());
        System.out.println("Client IP: " + clientIp);

        Bucket bucket = buckets.computeIfAbsent(
                clientIp,
                ignored -> createBucket()
        );

        System.out.println("Tracked clients: " + buckets.size());

        if (buckets.size() > MAX_TRACKED_CLIENTS) {
            buckets.remove(clientIp, bucket);
            writeRateLimitResponse(response);
            return;
        }

        if (bucket.tryConsume(1)) {
            System.out.println("TOKEN CONSUMED");
            filterChain.doFilter(request, response);
            return;
        }

        System.out.println("RATE LIMIT EXCEEDED");
        writeRateLimitResponse(response);
    }

    private Bucket createBucket() {

        Bandwidth limit = Bandwidth.classic(
                RateLimitPolicy.CAPACITY,
                Refill.greedy(
                        RateLimitPolicy.REFILL_TOKENS,
                        java.time.Duration.ofSeconds(
                                RateLimitPolicy.REFILL_PERIOD_SECONDS
                        )
                )
        );

        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    private void writeRateLimitResponse(
            HttpServletResponse response
    ) throws IOException {

        response.setStatus(429);
        response.setContentType("application/problem+json");
        response.setCharacterEncoding("UTF-8");

        response.setHeader("Retry-After", "60");

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
