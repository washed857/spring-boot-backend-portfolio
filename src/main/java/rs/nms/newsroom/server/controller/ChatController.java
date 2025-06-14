package rs.nms.newsroom.server.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import rs.nms.newsroom.server.dto.ChatDTOs;
import rs.nms.newsroom.server.service.ChatService;

import java.util.List;

/**
 * REST controller for chat rooms and messaging.
 * <p>
 * Provides endpoints for creating chat rooms, sending and retrieving messages, and viewing user participation.
 * All operations are secured and changes are audit-logged.
 * </p>
 */
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Tag(
    name = "Chat",
    description = "REST API for chat rooms and real-time messaging."
)
public class ChatController {

    private final ChatService chatService;

    @Operation(
        summary = "Create a new chat room",
        description = "Creates a new chat room and returns room details. The creator automatically becomes a participant."
    )
    @PostMapping("/rooms")
    public ResponseEntity<ChatDTOs.ChatRoomResponse> createRoom(
            @Valid @RequestBody ChatDTOs.CreateRoomRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return ResponseEntity.ok(chatService.createRoom(request, userId));
    }

    @Operation(
        summary = "Get all chat rooms for the current user",
        description = "Returns a list of all chat rooms where the authenticated user is a participant."
    )
    @GetMapping("/rooms")
    public ResponseEntity<List<ChatDTOs.ChatRoomResponse>> getUserRooms(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return ResponseEntity.ok(chatService.getUserRooms(userId));
    }

    @Operation(
        summary = "Send a message to a chat room",
        description = "Sends a message to the selected chat room. Only allowed if the user is a participant."
    )
    @PostMapping("/messages")
    public ResponseEntity<ChatDTOs.ChatMessageResponse> sendMessage(
            @Valid @RequestBody ChatDTOs.SendMessageRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return ResponseEntity.ok(chatService.sendMessage(request, userId));
    }

    @Operation(
        summary = "Get messages in a chat room (paginated)",
        description = "Returns a paginated list of messages from a given chat room, accessible only to participants."
    )
    @GetMapping("/rooms/{roomId}/messages")
    public ResponseEntity<Page<ChatDTOs.ChatMessageResponse>> getRoomMessages(
            @PathVariable Long roomId,
            Pageable pageable,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return ResponseEntity.ok(chatService.getRoomMessages(roomId, pageable, userId));
    }
}