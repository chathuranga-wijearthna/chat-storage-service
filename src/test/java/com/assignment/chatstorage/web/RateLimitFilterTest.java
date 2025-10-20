package com.assignment.chatstorage.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.assignment.chatstorage.config.RateLimitProperties;
import com.assignment.chatstorage.constants.HeaderConstants;
import com.assignment.chatstorage.dto.Dtos;
import com.assignment.chatstorage.dto.Dtos.SessionView;
import com.assignment.chatstorage.security.RateLimitFilter;
import com.assignment.chatstorage.service.ChatService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class RateLimitFilterTest {

    private MockMvc mockMvc;
    private ChatService chatService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        // Mock service and controller
        chatService = Mockito.mock(ChatService.class);
        ChatController controller = new ChatController(chatService);

        // Configure a strict rate limit: 2 requests per long window
        RateLimitProperties props = new RateLimitProperties();
        props.setEnabled(true);
        props.setCapacity(2);
        props.setRefillTokens(2);
        props.setRefillPeriodSeconds(3600);
        props.setPerApiKey(true);
        props.setIncludeHeaders(true);

        RateLimitFilter rateLimitFilter = new RateLimitFilter(props);

        this.mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .addFilter(rateLimitFilter)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        this.objectMapper = new ObjectMapper();

        Mockito.when(chatService.createSession(Mockito.any()))
                .thenReturn(new SessionView(null, "u1", "t", false, null, null));
    }

    @Test
    @DisplayName("exceeding rate limit returns 429 with headers")
    void rateLimit_exceeded_429() throws Exception {
        var req = new Dtos.SessionCreate("u1", "t");

        // First request OK
        mockMvc.perform(post("/api/v1/session")
                        .header(HeaderConstants.API_KEY, "change-me-please")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(header().string(HeaderConstants.RATE_LIMIT_LIMIT, "2"))
                .andExpect(header().string(HeaderConstants.RATE_LIMIT_REMAINING, "1"));

        // Second request OK
        mockMvc.perform(post("/api/v1/session")
                        .header(HeaderConstants.API_KEY, "change-me-please")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(header().string(HeaderConstants.RATE_LIMIT_LIMIT, "2"))
                .andExpect(header().string(HeaderConstants.RATE_LIMIT_REMAINING, "0"));

        // Third request should be blocked
        mockMvc.perform(post("/api/v1/session")
                        .header(HeaderConstants.API_KEY, "change-me-please")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string(HeaderConstants.RATE_LIMIT_LIMIT, "2"))
                .andExpect(header().string(HeaderConstants.RATE_LIMIT_REMAINING, "0"))
                .andExpect(header().exists(HeaderConstants.RETRY_AFTER))
                .andExpect(jsonPath("$.code").value("ERR_CS_RATE_01"))
                .andExpect(jsonPath("$.message").value("Too many requests"));
    }
}
