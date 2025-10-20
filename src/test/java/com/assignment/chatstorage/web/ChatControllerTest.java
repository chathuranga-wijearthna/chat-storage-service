package com.assignment.chatstorage.web;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.assignment.chatstorage.dto.Dtos;
import com.assignment.chatstorage.dto.Dtos.MessageView;
import com.assignment.chatstorage.dto.Dtos.SessionView;
import com.assignment.chatstorage.service.ChatService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class ChatControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ChatService chatService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        ChatController controller = new ChatController(chatService);

        this.mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        this.objectMapper = new ObjectMapper();
    }

    private static final String API_KEY = "change-me-please";
    private static final UUID ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Test
    @DisplayName("create returns 200 with body when valid")
    void create_ok() throws Exception {
        var req = new Dtos.SessionCreate("u1", "t");
        given(chatService.createSession(any())).willReturn(new SessionView(null, "u1", "t", false, null, null));

        mockMvc.perform(post("/api/v1/session")
                        .header("X-API-KEY", API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("u1"));
    }

    @Test
    @DisplayName("list returns sessions for user")
    void list_ok() throws Exception {
        given(chatService.listSessions("u", null)).willReturn(List.of());

        mockMvc.perform(get("/api/v1/session").header("X-API-KEY", API_KEY).param("userId", "u"))
                .andExpect(status().isOk())
                .andExpect(content().string("[]"));
    }

    @Test
    @DisplayName("rename updates title")
    void rename_ok() throws Exception {
        given(chatService.rename(eq(ID), any())).willReturn(new SessionView(null, "u", "n", false, null, null));

        mockMvc.perform(patch("/api/v1/session/{id}/rename", ID.toString())
                        .header("X-API-KEY", API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"n\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("n"));
    }

    @Test
    @DisplayName("favorite toggles flag")
    void favorite_ok() throws Exception {
        given(chatService.favorite(eq(ID), any())).willReturn(new SessionView(null, "u", "t", true, null, null));

        mockMvc.perform(patch("/api/v1/session/{id}/favorite", ID.toString())
                        .header("X-API-KEY", API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"isFavorite\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isFavorite").value(true));
    }

    @Test
    @DisplayName("delete returns 204")
    void delete_ok() throws Exception {
        mockMvc.perform(delete("/api/v1/session/{id}", ID.toString()).header("X-API-KEY", API_KEY))
                .andExpect(status().isNoContent());
        Mockito.verify(chatService).deleteSession(ID);
    }

    @Test
    @DisplayName("addMessage returns message view")
    void addMessage_ok() throws Exception {
        given(chatService.addMessage(eq(ID), any())).willReturn(new MessageView(null, null, "user", "c", null, null));

        mockMvc.perform(post("/api/v1/session/{id}/messages", ID.toString())
                        .header("X-API-KEY", API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sender\":\"user\",\"content\":\"c\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sender").value("user"));
    }

    @Test
    @DisplayName("listMessages returns a page structure")
    void listMessages_ok() throws Exception {

        var content = List.of(
                new MessageView(null, null, "assistant", "c1", null, null),
                new MessageView(null, null, "user", "c2", null, null)
        );

        var page = new PageImpl<>(content, PageRequest.of(0, 3), 2);

        given(chatService.listMessages(eq(ID), eq(0), eq(3))).willReturn(page);

        mockMvc.perform(get("/api/v1/session/{id}/messages", ID.toString())
                        .header("X-API-KEY", API_KEY)
                        .param("page", "0")
                        .param("size", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(3))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].sender").value("assistant"))
                .andExpect(jsonPath("$.content[1].sender").value("user"));
    }
}

