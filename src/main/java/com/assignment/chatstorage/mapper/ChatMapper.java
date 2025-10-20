package com.assignment.chatstorage.mapper;

import com.assignment.chatstorage.dto.Dtos;
import com.assignment.chatstorage.dto.Dtos.MessageView;
import com.assignment.chatstorage.dto.Dtos.SessionView;
import com.assignment.chatstorage.entity.ChatMessage;
import com.assignment.chatstorage.entity.ChatSession;
import io.micrometer.common.util.StringUtils;

public class ChatMapper {

    public static ChatSession toSessionEntity(Dtos.SessionCreate request) {

        ChatSession session = new ChatSession();
        session.setUserId(request.userId());
        session.setTitle(StringUtils.isBlank(request.title()) ? "New Chat" : request.title());

        return session;
    }

    public static SessionView toSessionView(ChatSession session) {
        return new SessionView(session.getId(), session.getUserId(), session.getTitle(), session.getFavorite(), session.getCreatedAt(),
                session.getUpdatedAt());
    }

    public static ChatMessage toChatEntity(Dtos.MessageCreate request, ChatSession session) {

        ChatMessage message = new ChatMessage();
        message.setSession(session);
        message.setSender(request.sender());
        message.setContent(request.content());
        message.setContextJson(request.context());

        return message;
    }

    public static MessageView toMessageView(ChatMessage message) {
        return new MessageView(message.getId(), message.getSession().getId(), message.getSender(), message.getContent(), message.getContextJson(),
                message.getCreatedAt());
    }

}
