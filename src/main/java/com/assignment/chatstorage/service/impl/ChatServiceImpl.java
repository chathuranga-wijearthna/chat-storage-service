package com.assignment.chatstorage.service.impl;


import com.assignment.chatstorage.dto.Dtos.MessageCreate;
import com.assignment.chatstorage.dto.Dtos.MessageView;
import com.assignment.chatstorage.dto.Dtos.SessionCreate;
import com.assignment.chatstorage.dto.Dtos.SessionFavorite;
import com.assignment.chatstorage.dto.Dtos.SessionRename;
import com.assignment.chatstorage.dto.Dtos.SessionView;
import com.assignment.chatstorage.entity.ChatMessage;
import com.assignment.chatstorage.entity.ChatSession;
import com.assignment.chatstorage.exception.CustomGlobalException;
import com.assignment.chatstorage.exception.ErrorCode;
import com.assignment.chatstorage.mapper.ChatMapper;
import com.assignment.chatstorage.repository.ChatMessageRepository;
import com.assignment.chatstorage.repository.ChatSessionRepository;
import com.assignment.chatstorage.service.ChatService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatSessionRepository sessionRepo;
    private final ChatMessageRepository messageRepo;

    @Override
    @Transactional
    public SessionView createSession(SessionCreate req) {
        log.info("Creating chat session for userId={}", req.userId());
        ChatSession session = ChatMapper.toSessionEntity(req);
        session = sessionRepo.save(session);
        log.debug("Created session id={}", session.getId());
        return ChatMapper.toSessionView(session);
    }

    @Override
    public List<SessionView> listSessions(String userId, Boolean favorite) {
        log.info("Listing sessions for userId={} favorite={}", userId, favorite);
        List<ChatSession> list = (favorite == null) ? sessionRepo.findByUserIdAndIsDeletedFalseOrderByUpdatedAtDesc(userId)
                : sessionRepo.findByUserIdAndFavoriteAndIsDeletedFalseOrderByUpdatedAtDesc(userId, favorite);
        return list.stream().map(ChatMapper::toSessionView).toList();
    }

    @Override
    @Transactional
    public SessionView rename(UUID id, SessionRename req) {
        log.info("Renaming session id={}", id);
        ChatSession session = sessionRepo.findById(id).orElseThrow(() -> new CustomGlobalException(ErrorCode.SESSION_NOT_FOUND));
        session.setTitle(req.title());
        session = sessionRepo.save(session);
        log.debug("Renamed session id={} newTitle={}", id, req.title());
        return ChatMapper.toSessionView(session);
    }

    @Override
    @Transactional
    public SessionView favorite(UUID id, SessionFavorite req) {
        log.info("Updating favorite for session id={} favorite={}", id, req.isFavorite());
        ChatSession session = sessionRepo.findById(id).orElseThrow(() -> new CustomGlobalException(ErrorCode.SESSION_NOT_FOUND));
        session.setFavorite(req.isFavorite());
        session = sessionRepo.save(session);
        log.debug("Updated favorite id={} favorite={}", id, req.isFavorite());
        return ChatMapper.toSessionView(session);
    }

    @Override
    @Transactional
    public void deleteSession(UUID id) {
        log.info("Soft-deleting session id={}", id);
        ChatSession session = sessionRepo.findById(id).orElseThrow(() -> new CustomGlobalException(ErrorCode.SESSION_NOT_FOUND));
        session.setIsDeleted(true);
        sessionRepo.save(session);
    }

    @Override
    @Transactional
    public MessageView addMessage(UUID sessionId, MessageCreate req) {
        log.info("Adding message to session id={}", sessionId);
        ChatSession session = sessionRepo.findById(sessionId).orElseThrow(() -> new CustomGlobalException(ErrorCode.SESSION_NOT_FOUND));
        ChatMessage message = ChatMapper.toChatEntity(req, session);
        message = messageRepo.save(message);
        log.debug("Saved message id={} for session id={}", message.getId(), sessionId);
        return ChatMapper.toMessageView(message);
    }

    @Override
    public Page<MessageView> listMessages(UUID sessionId, int page, int size) {
        log.debug("Listing messages for session id={} page={} size={}", sessionId, page, size);

        var pageable = PageRequest.of(Math.max(0, page), Math.max(1, size)).withSort(Sort.Direction.ASC, "createdAt");

        Page<ChatMessage> items = messageRepo.findBySessionId(sessionId, pageable);
        return items.map(ChatMapper::toMessageView);
    }
}
