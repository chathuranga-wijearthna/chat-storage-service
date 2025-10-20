package com.assignment.chatstorage.config;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "ratelimit")
public class RateLimitProperties {

    /** Enable/disable rate limiting globally */
    private boolean enabled = true;

    /** Maximum tokens in the bucket (max requests) */
    private int capacity = 100;

    /** Number of tokens to refill each period */
    private int refillTokens = 100;

    /** Refill period in seconds */
    private int refillPeriodSeconds = 60;

    /** If true, identify bucket by X-API-KEY; otherwise by client IP */
    private boolean perApiKey = true;

    /** Whether to include rate limit headers in responses */
    private boolean includeHeaders = true;

    /** Paths to skip filtering (prefix match) */
    private List<String> skipPaths;
}

