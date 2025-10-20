package com.assignment.chatstorage.service;

import com.assignment.chatstorage.dto.Dtos.MessageCreate;
import com.assignment.chatstorage.dto.Dtos.MessageView;
import com.assignment.chatstorage.dto.Dtos.SessionCreate;
import com.assignment.chatstorage.dto.Dtos.SessionFavorite;
import com.assignment.chatstorage.dto.Dtos.SessionRename;
import com.assignment.chatstorage.dto.Dtos.SessionView;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;

public interface ChatService {

    SessionView createSession(SessionCreate req);

    List<SessionView> listSessions(String userId, Boolean favorite);

    SessionView rename(UUID id, SessionRename req);

    SessionView favorite(UUID id, SessionFavorite req);

    void deleteSession(UUID id);

    MessageView addMessage(UUID sessionId, MessageCreate req);

    Page<MessageView> listMessages(UUID sessionId, int page, int size);
}
