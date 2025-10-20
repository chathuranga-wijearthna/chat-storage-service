package com.assignment.chatstorage.web;

import com.assignment.chatstorage.dto.Dtos;
import com.assignment.chatstorage.dto.Dtos.MessageView;
import com.assignment.chatstorage.dto.Dtos.SessionFavorite;
import com.assignment.chatstorage.dto.Dtos.SessionView;
import com.assignment.chatstorage.dto.PageResponse;
import com.assignment.chatstorage.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/session")
@Tag(name = "RAG Chat Storage API")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService service;

    @PostMapping()
    @Operation(summary = "Create chat session")
    public SessionView create(@Valid @RequestBody Dtos.SessionCreate req) {
        log.info("create session for userId={}", req.userId());
        return service.createSession(req);
    }

    @GetMapping()
    @Operation(summary = "List chat sessions")
    public List<SessionView> list(@RequestParam String userId, @RequestParam(required = false) Boolean favorite) {
        log.info("getting list of sessions userId={} favorite={}", userId, favorite);
        return service.listSessions(userId, favorite);
    }

    @PatchMapping("/{id}/rename")
    @Operation(summary = "Rename chat session")
    public SessionView rename(@PathVariable UUID id, @Valid @RequestBody Dtos.SessionRename req) {
        log.info("rename session id={}", id);
        return service.rename(id, req);
    }

    @PatchMapping("/{id}/favorite")
    @Operation(summary = "Mark or unmark favorite")
    public SessionView markFavorite(@PathVariable UUID id, @RequestBody SessionFavorite req) {
        log.info("favorite session id={} favorite={}", id, req.isFavorite());
        return service.favorite(id, req);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft delete chat session")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        log.info("Delete session id={}", id);
        service.deleteSession(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/messages")
    @Operation(summary = "Add message to a session")
    public MessageView addMessage(@PathVariable UUID id, @Valid @RequestBody Dtos.MessageCreate req) {
        log.info("add message to session id={}", id);
        return service.addMessage(id, req);
    }

    @GetMapping("/{id}/messages")
    @Operation(summary = "List messages of a session")
    public PageResponse<MessageView> listMessages(@PathVariable UUID id, @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.debug("list messages session id={} page={} size={}", id, page, size);
        var messages = service.listMessages(id, page, size);
        return new PageResponse<>(messages.getContent(), messages.getNumber(), messages.getSize(),
                messages.getTotalElements(),
                messages.getTotalPages());
    }
}
