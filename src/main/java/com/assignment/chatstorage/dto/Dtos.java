package com.assignment.chatstorage.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.UUID;

public class Dtos {

    public record SessionCreate(@NotBlank String userId, String title) {

    }

    public record SessionRename(@NotBlank String title) {

    }

    public record SessionFavorite(boolean isFavorite) {

    }

    public record SessionView(UUID id, String userId, String title, boolean isFavorite, LocalDateTime createdAt, LocalDateTime updatedAt) {

    }

    public record MessageCreate(@NotBlank String createdBy, @NotBlank String content, String context) {

    }

    public record MessageView(UUID id, UUID sessionId, String createdBy, String content, String context, LocalDateTime createdAt) {

    }
}
