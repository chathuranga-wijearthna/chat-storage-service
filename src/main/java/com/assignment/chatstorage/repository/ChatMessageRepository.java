package com.assignment.chatstorage.repository;

import com.assignment.chatstorage.entity.ChatMessage;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {

    Page<ChatMessage> findBySessionId(UUID sessionId, Pageable pageable);
}
