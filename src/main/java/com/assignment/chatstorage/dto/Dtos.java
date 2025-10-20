package com.assignment.chatstorage.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.time.OffsetDateTime;
import java.util.UUID;

public class Dtos {

    public record SessionCreate(@NotBlank String userId, String title) {

    }

    public record SessionRename(@NotBlank String title) {

    }

    public record SessionFavorite(boolean isFavorite) {

    }

    public record SessionView(UUID id, String userId, String title, boolean isFavorite, OffsetDateTime createdAt, OffsetDateTime updatedAt) {

    }

    public record MessageCreate(@Pattern(regexp = "^(user|assistant)$") String sender, @NotBlank String content, String context) {

    }

    public record MessageView(UUID id, UUID sessionId, String sender, String content, String context, OffsetDateTime createdAt) {

    }
}
