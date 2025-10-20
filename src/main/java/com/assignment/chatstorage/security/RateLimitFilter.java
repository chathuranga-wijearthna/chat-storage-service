package com.assignment.chatstorage.security;

import com.assignment.chatstorage.config.RateLimitProperties;
import com.assignment.chatstorage.exception.ErrorCode;
import com.assignment.chatstorage.constants.HeaderConstants;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@Order(2)
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitProperties props;

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {

        if (!props.isEnabled()) {
            return true;
        }

        String uri = request.getRequestURI();
        List<String> skip = props.getSkipPaths();
        return skip != null && skip.stream().anyMatch(uri::startsWith);
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        Bucket bucket = resolveBucket(request);
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            if (props.isIncludeHeaders()) {
                addHeaders(response, probe.getRemainingTokens());
            }
            filterChain.doFilter(request, response);
        } else {

            long nanosToWait = probe.getNanosToWaitForReset();
            long secondsToWait = (long) Math.ceil(nanosToWait / 1_000_000_000.0);

            if (props.isIncludeHeaders()) {
                addHeaders(response, 0);
                response.setHeader(HeaderConstants.RETRY_AFTER, String.valueOf(secondsToWait));
            }

            String body = String.format("{\"code\":\"%s\",\"message\":\"%s\"}",
                    ErrorCode.RATE_LIMIT_EXCEEDED.getCode(), ErrorCode.RATE_LIMIT_EXCEEDED.getDescription());

            // 429 Too Many Requests
            response.setStatus(429);
            response.setContentType("application/json");
            response.getWriter().write(body);

            log.warn("Rate limit exceeded for key={} uri={} waitSeconds={}", keyFrom(request), request.getRequestURI(), secondsToWait);
        }
    }

    private void addHeaders(HttpServletResponse response, long remaining) {
        response.setHeader(HeaderConstants.RATE_LIMIT_LIMIT, String.valueOf(props.getCapacity()));
        response.setHeader(HeaderConstants.RATE_LIMIT_REMAINING, String.valueOf(remaining));
    }

    private Bucket resolveBucket(HttpServletRequest request) {
        String key = keyFrom(request);
        return buckets.computeIfAbsent(key, k -> buildBucket());
    }

    private String keyFrom(HttpServletRequest request) {
        if (props.isPerApiKey()) {
            String apiKey = request.getHeader(HeaderConstants.API_KEY);
            if (apiKey != null && !apiKey.isBlank()) {
                return "api:" + apiKey;
            }
        }
        String forwarded = request.getHeader("X-Forwarded-For");
        String ip = (forwarded != null && !forwarded.isBlank()) ? forwarded.split(",")[0].trim() : request.getRemoteAddr();
        return "ip:" + (ip == null ? "unknown" : ip);
    }

    private Bucket buildBucket() {
        Bandwidth limit = Bandwidth.builder()
                .capacity(props.getCapacity())
                .refillGreedy(props.getRefillTokens(), Duration.ofSeconds(props.getRefillPeriodSeconds()))
                .build();
        return Bucket.builder().addLimit(limit).build();
    }
}
