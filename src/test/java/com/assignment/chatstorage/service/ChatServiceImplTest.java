package com.assignment.chatstorage.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.assignment.chatstorage.dto.Dtos;
import com.assignment.chatstorage.entity.ChatMessage;
import com.assignment.chatstorage.entity.ChatSession;
import com.assignment.chatstorage.exception.CustomGlobalException;
import com.assignment.chatstorage.repository.ChatMessageRepository;
import com.assignment.chatstorage.repository.ChatSessionRepository;
import com.assignment.chatstorage.service.impl.ChatServiceImpl;
import java.util.List;
import java.util.UUID;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.mockito.ArgumentCaptor;

@ExtendWith(MockitoExtension.class)
class ChatServiceImplTest {

    private static final UUID ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID SID = UUID.fromString("00000000-0000-0000-0000-000000000002");

    @Mock
    ChatSessionRepository sessionRepository;
    @Mock
    ChatMessageRepository messageRepository;

    @InjectMocks
    ChatServiceImpl service;

    @Test
    @DisplayName("createSession saves and returns view")
    void createSession_ok() {
        var req = new Dtos.SessionCreate("u1", "Welcome");
        var saved = new ChatSession();
        saved.setUserId("u1");
        saved.setTitle("Welcome");
        given(sessionRepository.save(any(ChatSession.class))).willReturn(saved);

        var view = service.createSession(req);

        assertThat(view.userId()).isEqualTo("u1");
        assertThat(view.title()).isEqualTo("Welcome");
        verify(sessionRepository).save(any(ChatSession.class));
    }

    @Test
    @DisplayName("listSessions filters by favorite when provided")
    void listSessions_withFavorite() {
        var s = new ChatSession();
        s.setUserId("u");
        given(sessionRepository.findByUserIdAndFavoriteAndIsDeletedFalseOrderByUpdatedAtDesc("u", true))
                .willReturn(List.of(s));

        var list = service.listSessions("u", true);

        assertThat(list).hasSize(1);
        verify(sessionRepository).findByUserIdAndFavoriteAndIsDeletedFalseOrderByUpdatedAtDesc("u", true);
    }

    @Test
    @DisplayName("listSessions uses all sessions when favorite is null")
    void listSessions_allWhenNull() {
        given(sessionRepository.findByUserIdAndIsDeletedFalseOrderByUpdatedAtDesc("u")).willReturn(List.of());

        var list = service.listSessions("u", null);

        assertThat(list).isEmpty();
        verify(sessionRepository).findByUserIdAndIsDeletedFalseOrderByUpdatedAtDesc("u");
    }

    @Test
    @DisplayName("rename updates title and saves")
    void rename_ok() {
        var s = new ChatSession();
        s.setTitle("Old");
        given(sessionRepository.findById(ID)).willReturn(Optional.of(s));
        given(sessionRepository.save(s)).willReturn(s);

        var view = service.rename(ID, new Dtos.SessionRename("New"));

        assertThat(view.title()).isEqualTo("New");
        verify(sessionRepository).save(s);
    }

    @Test
    @DisplayName("rename throws when session not found")
    void rename_notFound() {
        given(sessionRepository.findById(ID)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.rename(ID, new Dtos.SessionRename("x")))
                .isInstanceOf(CustomGlobalException.class);
    }

    @Test
    @DisplayName("favorite toggles and saves")
    void favorite_ok() {
        var s = new ChatSession();
        given(sessionRepository.findById(ID)).willReturn(Optional.of(s));
        given(sessionRepository.save(s)).willReturn(s);

        var view = service.favorite(ID, new Dtos.SessionFavorite(true));

        // Assert
        assertThat(view.isFavorite()).isTrue();
        verify(sessionRepository).save(s);
    }

    @Test
    @DisplayName("deleteSession sets isDeleted true and saves")
    void deleteSession_ok() {
        var s = new ChatSession();
        given(sessionRepository.findById(ID)).willReturn(Optional.of(s));

        service.deleteSession(ID);

        assertThat(s.getIsDeleted()).isTrue();
        verify(sessionRepository).save(s);
    }

    @Test
    @DisplayName("addMessage saves and returns view")
    void addMessage_ok() {
        var s = new ChatSession();
        given(sessionRepository.findById(SID)).willReturn(Optional.of(s));
        given(messageRepository.save(any(ChatMessage.class))).willAnswer(inv -> inv.getArgument(0));

        var req = new Dtos.MessageCreate("user", "hello", null);

        var view = service.addMessage(SID, req);

        assertThat(view.sender()).isEqualTo("user");
        assertThat(view.content()).isEqualTo("hello");
        verify(messageRepository).save(any(ChatMessage.class));
    }

    @Test
    @DisplayName("listMessages maps page and passes pageable with sort ASC by createdAt")
    void listMessages_ok() {
        
        var m1 = new ChatMessage();
        m1.setSender("user");
        m1.setContent("a");
        
        var m2 = new ChatMessage();
        m2.setSender("assistant");
        m2.setContent("b");
        
        var sess = new ChatSession();
        m1.setSession(sess);
        m2.setSession(sess);

        var repoPage = new PageImpl<>(List.of(m1, m2));
        given(messageRepository.findBySessionId(eq(SID), any(Pageable.class))).willReturn(repoPage);

        var result = service.listMessages(SID, 2, 5);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).content()).isEqualTo("a");
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(messageRepository).findBySessionId(eq(SID), captor.capture());
        var pageable = captor.getValue();
        assertThat(pageable.getPageNumber()).isEqualTo(2);
        assertThat(pageable.getPageSize()).isEqualTo(5);
        assertThat(pageable.getSort().getOrderFor("createdAt").getDirection()).isEqualTo(Sort.Direction.ASC);
    }
}


