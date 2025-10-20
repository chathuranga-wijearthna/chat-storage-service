package com.assignment.chatstorage.repository;


import com.assignment.chatstorage.entity.ChatSession;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, UUID> {

    List<ChatSession> findByUserIdAndIsDeletedFalseOrderByUpdatedAtDesc(String userId);

    List<ChatSession> findByUserIdAndFavoriteAndIsDeletedFalseOrderByUpdatedAtDesc(String userId, boolean favorite);
}
